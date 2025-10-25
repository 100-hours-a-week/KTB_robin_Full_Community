package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.*;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.image.ImageProcessor;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.posts.Comment;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import ktb3.fullstack.week4.repository.posts.CommentRepository;
import ktb3.fullstack.week4.repository.posts.LikeRepository;
import ktb3.fullstack.week4.repository.posts.PostRepository;
import ktb3.fullstack.week4.repository.UserRepository;
import ktb3.fullstack.week4.repository.posts.PostViewRepository;
import ktb3.fullstack.week4.store.images.PostImageStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageProcessor imageProcessor;
    private final PostImageStore postImageStore;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;

    // 게시글 등록
    public void uploadPost(long userId, PostUploadRequeset dto, MultipartFile image) {
        checkCanNotFoundUser(userId);

        String postImageUrl = null;
        if(image != null) {
            byte[] imageBytes = imageProcessor.toByteStream(image);
            postImageUrl = imageProcessor.makeRandomImageUrl();
            postImageStore.uploadImage(postImageUrl, imageBytes);
        }

        long authorId = userId;
        String title = dto.getTitle();
        String content = dto.getContent();

        Post post = new Post(
                0L,
                authorId,
                title,
                content,
                postImageUrl,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        // 이미 존재하는 유저라면
        if(post.getId() != 0) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        postRepository.save(post);
    }

    // 게시글 목록조회
    public PostListResponse getPostList(long userId, int after, int limit) {
        checkCanNotFoundUser(userId);

        int from = after + 1;
        int to = after + limit;

        List<Post> raw = postRepository.findPosts(from, to);

        boolean hasNext = raw.size() > limit;
        List<Post> page = hasNext ? raw.subList(0, limit) : raw;

        List<PostListResponse.PostBriefInfo> briefs = new ArrayList<>(page.size());

        for (Post post : page) {
            long likes = likeRepository.countByPostId(post.getId());
            long comments = commentRepository.countByPostId(post.getId());
            long views = postViewRepository.countByPostId(post.getId());

            String author = userRepository.findById(post.getAuthorId())
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
        checkCanNotFoundUser(userId);

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
        checkCanNotFoundUser(userId);

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
        checkCanNotFoundUser(userId);

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


    // 게시글 좋아요
    public void likePost(long userId, long postId) {
        checkCanNotFoundUser(userId);
        Post post = checkCanNotFoundPost(postId);
        likeRepository.like(post.getId(), userId);
    }

    // 게시글 좋아요 취소
    public void unlikePost(long userId, long postId) {
        checkCanNotFoundUser(userId);
        Post post = checkCanNotFoundPost(postId);
        likeRepository.unlike(post.getId(), userId);
    }

    // 댓글 등록
    public void addComment(long userId, long postId, String content) {
        checkCanNotFoundUser(userId);
        Post post = checkCanNotFoundPost(postId);
        if (content == null || content.isBlank()) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }
        commentRepository.addComment(post.getId(), userId, content);
    }

    // 댓글 수정
    public void editComment(long userId, long postId, long commentId, String content) {
        checkCanNotFoundUser(userId);
        checkCanNotFoundPost(postId);
        if (content == null || content.isBlank()) {
            throw new ApiException(GenericError.INVALID_REQUEST);
        }

        // 존재/소유 확인을 위해 먼저 조회
        List<Comment> comments = commentRepository.findByPostId(postId);
        Comment target = null;
        for (Comment c : comments) {
            if (c.getId() == commentId) {
                target = c;
                break;
            }
        }
        if (target == null) {
            throw new ApiException(CommentError.CANNOT_FOUND_COMMENT);
        }
        if (target.getAuthorId() != userId) {
            throw new ApiException(CommentError.CANNOT_EDIT_OTHERS_COMMENT);
        }

        boolean ok = commentRepository.editComment(postId, commentId, userId, content);
        if (!ok) {
            // 이 경우는 동시성 등으로 수정 실패한 예외 케이스
            throw new ApiException(CommentError.CANNOT_FOUND_COMMENT);
        }
    }

    // 댓글 삭제
    public void removeComment(long userId, long postId, long commentId) {
        checkCanNotFoundUser(userId);
        checkCanNotFoundPost(postId);

        // 존재/소유 확인을 위해 먼저 조회
        List<Comment> comments = commentRepository.findByPostId(postId);
        Comment target = null;
        for (Comment comment : comments) {
            if (comment.getId() == commentId) {
                target = comment;
                break;
            }
        }

        if (target == null) {
            throw new ApiException(CommentError.CANNOT_FOUND_COMMENT);
        }
        if (target.getAuthorId() != userId) {
            throw new ApiException(CommentError.CANNOT_DELETE_OTHERS_COMMENT);
        }

        boolean ok = commentRepository.deleteComment(postId, commentId, userId);
        if (!ok) {
            throw new ApiException(CommentError.CANNOT_FOUND_COMMENT);
        }
    }

    private User checkCanNotFoundUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        return user;
    }

    private Post checkCanNotFoundPost(long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(PostError.CANNOT_FOUND_POST));
        return post;
    }

    private void checkCanNotEditOthersPost(long userId, long authorId) {
        if(userId != authorId) {
            throw new ApiException(PostError.CANNOT_EDIT_OTHERS_POST);
        }
    }

    private void checkCanNotDeleteOthersPost(long userId, long authorId) {
        if(userId != authorId) {
            throw new ApiException(PostError.CANNOT_DELETE_OTHERS_POST);
        }
    }
}
