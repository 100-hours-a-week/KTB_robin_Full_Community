package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.*;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.image.ImageProcessor;
import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import ktb3.fullstack.week4.repository.comments.CommentRepository;
import ktb3.fullstack.week4.repository.likes.LikeRepository;
import ktb3.fullstack.week4.repository.posts.PostRepository;
import ktb3.fullstack.week4.repository.users.UserRepository;
import ktb3.fullstack.week4.repository.posts.PostViewRepository;
import ktb3.fullstack.week4.service.error.ErrorCheckServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final CommentService commentService;
    private final LikeService likeService;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;
    private final ErrorCheckServiceImpl errorCheckService;

    @Value("${file.postDir}")
    private String folderPath;

    // 게시글 등록
    @Transactional
    public void uploadPost(long userId, PostUploadRequeset dto, MultipartFile image) {

        User user = errorCheckService.checkCanNotFoundUser(userId, userRepository);

        String postImageUrl = folderPath + image.getOriginalFilename();

        // (리팩토링 필요) 에러 등록 및 관리
        try {
            image.transferTo(new File(postImageUrl));
        } catch (IOException e) {
            System.out.println("이미지 이동 중 문제 발생!");
            log.info("이미지 이동 중 문제 발생!");
        }

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .postView(null)
                .user(user)
                .build();

        // 이미 존재하는 유저라면
        if(post.getId() != null) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        PostImage postImage = PostImage.builder()
                .post(post)
                .imageUrl(postImageUrl)
                .build();

        post.addPostImages(postImage);

        postRepository.save(post);
    }

    // 게시글 목록조회
    @Transactional(readOnly = true)
    public PostListResponse getPostList(long userId, int after, int limit) {
        errorCheckService.checkCanNotFoundUser(userId, userRepository);

        int from = after + 1;
        int to = after + limit;

        List<Post> raw = postRepository.findAllByIdBetween((long)from, (long)to);

        boolean hasNext = raw.size() > limit;
        List<Post> page = hasNext ? raw.subList(0, limit) : raw;

        List<PostListResponse.PostBriefInfo> briefs = new ArrayList<>(page.size());

        for (Post post : page) {
            long likes = likeRepository.countByPostId(post.getId());
            long comments = commentRepository.countByPostId(post.getId());
            long views = postViewRepository.countByPostId(post.getId());

            String author = userRepository.findById(post.getUser().getId())
                    .map(User::getNickname)
                    .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));

            briefs.add(new PostListResponse.PostBriefInfo(
                    post.getId(),
                    post.getTitle(),
                    likes,
                    comments,
                    views,
                    author,
                    post.getModifiedAt()
            ));
        }

        int nextCursor = page.isEmpty() ?
                after :
                Math.toIntExact(page.get(page.size() - 1).getId());

        return new PostListResponse(briefs, nextCursor, hasNext);
    }

    // 게시글 상세조회
    public PostDetailResponse getSinglePostDeatil(long userId, long postId) {
        errorCheckService.checkCanNotFoundUser(userId, userRepository);

        if (postId <= 0) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }
        Post post = checkCanNotFoundPost(postId);

        // 조회수 증가 정책: 상세 조회 시 1 증가
        postViewRepository.plusViewCount(postId);

        User authorEntity = checkCanNotFoundUser(post.getAuthorId());
        String author = authorEntity.getNickname();

        // 숫자 정보
        long likes = likeRepository.countByPostId(postId);
        long commentsCount = commentRepository.countByPostId(postId);
        long views = postViewRepository.countByPostId(postId);

        // (사용자 - 현재 조회중인 게시물) 좋아요 누름 여부
        boolean isLiked = likeRepository.isLiked(postId, userId);

        // 댓글 상세 목록 조회 + dto 매핑
        List<Comment> commentEntities = commentRepository.findByPostId(postId);
        List<PostDetailResponse.CommentInfo> comments = new ArrayList<>();
        for (Comment comment : commentEntities) {
            User commentAuthorEntity = checkCanNotFoundUser(comment.getAuthorId());
            String commentAuthor = commentAuthorEntity.getNickname();

            comments.add(new PostDetailResponse.CommentInfo(
                            comment.getId(),
                            commentAuthor,
                            comment.getContent(),
                            comment.getModifiedAt()
                    )
            );
        }

        PostDetailResponse.PostInfo postInfo = new PostDetailResponse.PostInfo(
                post.getId(),
                post.getTitle(),
                likes,
                commentsCount,
                views,
                author,
                post.getModifiedAt(),
                post.getImageUrl(),
                post.getContent()
        );

        return new PostDetailResponse(postInfo, isLiked, comments);
    }

    // 게시글 수정
    public void editPost(long userId, long postId, PostUploadRequeset dto, MultipartFile image) {
        errorCheckService.checkCanNotFoundUser(userId, userRepository);

        Post post = checkCanNotFoundPost(postId);

        // 작성자 본인 여부 확인
        checkCanNotEditOthersPost(userId, post.getAuthorId());

        // 제목/본문 수정
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        // 이미지 수정: 새 이미지가 있을 때만 교체
        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            String existingUrl = post.getImageUrl();
            if (existingUrl != null) {
                postImageStore.deleteImage(existingUrl);
            }
            // 새 이미지 업로드
            byte[] imageBytes = imageProcessor.toByteStream(image);
            String newUrl = imageProcessor.makeRandomImageUrl();
            postImageStore.uploadImage(newUrl, imageBytes);
            post.setImageUrl(newUrl);
        }

        // 수정 시각 갱신
        post.setModifiedAt(LocalDateTime.now());

        // 저장
        postRepository.update(post);
    }

    // 게시글 삭제
    public void removePost(long userId, long postId) {
        errorCheckService.checkCanNotFoundUser(userId, userRepository);

        Post post = checkCanNotFoundPost(postId);

        // 본인 소유 확인
        checkCanNotDeleteOthersPost(userId, post.getAuthorId());

        // 게시글 이미지가 있다면 함께 정리
        String imageUrl = post.getImageUrl();
        if (imageUrl != null) {
            byte[] previousImageInfo = postImageStore.deleteImage(imageUrl);
            if(previousImageInfo == null) {
                throw new ApiException(FileError.IMAGE_NOT_FOUND);
            }
        }

        // 인메모리에서 삭제
        boolean isPostDeleted = postRepository.deleteById(postId);
        if (!isPostDeleted) {
            throw new ApiException(PostError.CANNOT_FOUND_POST);
        }
    }
}
