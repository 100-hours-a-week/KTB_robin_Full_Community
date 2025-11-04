package ktb3.fullstack.week4.service.posts;

import ktb3.fullstack.week4.common.error.codes.*;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.likes.Like;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.posts.PostView;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostEditRequest;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import ktb3.fullstack.week4.repository.comments.CommentRepository;
import ktb3.fullstack.week4.repository.images.PostImageRepository;
import ktb3.fullstack.week4.repository.likes.LikeRepository;
import ktb3.fullstack.week4.repository.posts.PostRepository;
import ktb3.fullstack.week4.repository.users.UserRepository;
import ktb3.fullstack.week4.repository.posts.PostViewRepository;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
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
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostDomainBuilder postDomainBuilder;

    private final ErrorCheckServiceImpl errorCheckService;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;
    private final PostImageRepository postImageRepository;

    private final PostDeleteFacade postDeleteFacade;

    @Value("${file.postDir}")
    private String folderPath;

    // 게시글 등록
    @Transactional
    public void uploadPost(long userId, PostUploadRequeset dto, MultipartFile image) {

        User user = errorCheckService.checkCanNotFoundUser(userId);

        String postImageUrl = folderPath + image.getOriginalFilename();

        // (리팩토링 필요) 에러 등록 및 관리
        try {
            image.transferTo(new File(postImageUrl));
        } catch (IOException e) {
            System.out.println("이미지 이동 중 문제 발생!");
            log.info("이미지 이동 중 문제 발생!");
        }

        Post post = postDomainBuilder.buildPost(dto, user);
        PostImage postImage = postDomainBuilder.buildPostImage(post, postImageUrl);
        postRepository.save(post);
        postImageRepository.save(postImage);
    }

    // 게시글 목록조회
    @Transactional(readOnly = true)
    public PostListResponse getPostList(long userId, int after, int limit) {
        errorCheckService.checkCanNotFoundUser(userId);

        int from = after + 1;
        int to = after + limit;

        List<Post> raw = postRepository.findAllByIdBetween((long) from, (long) to);

        boolean hasNext = raw.size() > limit;
        List<Post> page = hasNext ? raw.subList(0, limit) : raw;

        List<PostListResponse.PostBriefInfo> briefs = new ArrayList<>(page.size());


        for (Post post : page) {

            long postId = post.getId();

            long likes = likeRepository.countByPostId(postId);
            long comments = commentRepository.countByPostId(postId);
            long views = postViewRepository.countByPostId(postId);

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

        int nextCursor = page.isEmpty() ? after : Math.toIntExact(page.getLast().getId());

        return new PostListResponse(briefs, nextCursor, hasNext);
    }

    // 게시글 상세조회
    @Transactional
    public PostDetailResponse getSinglePostDeatil(long userId, long postId) {
        errorCheckService.checkCanNotFoundUser(userId);

        if (postId <= 0) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        Post post = errorCheckService.checkCanNotFoundPost(postId);

        // 조회 수 증가
        PostView postView = post.getPostView();
        postView.plusViewCount();

        User authorEntity = errorCheckService.checkCanNotFoundUser(post.getUser().getId());
        String author = authorEntity.getNickname();

        // 숫자 정보
        long likes = likeRepository.countByPostId(postId);
        long commentsCount = commentRepository.countByPostId(postId);
        long views = postViewRepository.countByPostId(postId);

        // (사용자 - 현재 조회중인 게시물) 좋아요 누름 여부
        boolean isLiked = likeRepository.existsByPostIdAndUserIdAndIsLikedTrue(postId, userId);

        // 댓글 상세 목록 조회 + dto 매핑
        List<Comment> commentEntities = commentRepository.findAllByPostId(postId);
        List<PostDetailResponse.CommentInfo> comments = new ArrayList<>();
        for (Comment comment : commentEntities) {

            comments.add(new PostDetailResponse.CommentInfo(
                            comment.getId(),
                            author,
                            comment.getContent(),
                            comment.getModifiedAt()
                    )
            );
        }

        String primaryImageUrl = "";
        try {
            PostImage primaryImage = postImageRepository.findByPostIdAndIsPrimaryIsTrue(postId).get();
            primaryImageUrl = primaryImage.getImageUrl();
        } catch (NoSuchElementException e) {
            System.out.println("사진이 없음");
            log.info("사진이 없음");
        }

        List<PostImage> restImages = postImageRepository.findAllNotPrimaryPostImages(postId);
        List<String> restImageUrls = new ArrayList<>();
        for (PostImage postImage : restImages) {
            restImageUrls.add(postImage.getImageUrl());
        }

        PostDetailResponse.PostInfo postInfo = new PostDetailResponse.PostInfo(
                post.getId(),
                post.getTitle(),
                likes,
                commentsCount,
                views,
                author,
                post.getModifiedAt(),
                primaryImageUrl,
                restImageUrls,
                post.getContent()
        );

        return new PostDetailResponse(postInfo, isLiked, comments);
    }

    // 게시글 수정
    @Transactional
    public void editPost(long userId, long postId, PostEditRequest dto, MultipartFile image) {
        errorCheckService.checkCanNotFoundUser(userId);

        Post post = errorCheckService.checkCanNotFoundPost(postId);

        // 작성자 본인 여부 확인
        errorCheckService.checkCanNotEditOthersPost(userId, post.getUser().getId());

        // 제목/본문 수정
        post.editPost(dto);

        // 이미지 수정: primary 이미지 교체
        if (image != null && !image.isEmpty()) {
            // 기존 대표 이미지 삭제
            postImageRepository.findByPostIdAndIsPrimaryIsTrue(postId)
                    .ifPresent(SoftDeletetionEntity::deleteEntity);

            // 2) 로컬 폴더에 새 이미지 파일 저장
            String postImageUrl = folderPath + image.getOriginalFilename();

            try {
                image.transferTo(new File(postImageUrl));
            } catch (IOException e) {
                System.out.println("이미지 이동 중 문제 발생!");
                log.info("이미지 이동 중 문제 발생!");
            }

            // 새 이미지 업로드
            PostImage newPrimaryImage = postDomainBuilder.buildPostImage(post, postImageUrl);
            postImageRepository.save(newPrimaryImage);
        }
    }

    // 게시글 삭제
    @Transactional
    public void removePost(long userId, long postId) {
        errorCheckService.checkCanNotFoundUser(userId);

        Post post = errorCheckService.checkCanNotFoundPost(postId);

        // 본인 소유 확인
        errorCheckService.checkCanNotDeleteOthersPost(userId, post.getUser().getId());

        postDeleteFacade.deletePost(postId);
    }
}
