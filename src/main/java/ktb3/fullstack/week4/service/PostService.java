package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.image.ImageProcessor;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.PostDetailResponse;
import ktb3.fullstack.week4.dto.posts.PostListResponse;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import ktb3.fullstack.week4.repository.posts.CommentRepository;
import ktb3.fullstack.week4.repository.posts.LikeRepository;
import ktb3.fullstack.week4.repository.posts.PostRepository;
import ktb3.fullstack.week4.repository.UserRepository;
import ktb3.fullstack.week4.repository.posts.ViewRepository;
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
    private final ViewRepository viewRepository;

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
            long views = viewRepository.countByPostId(post.getId());

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
