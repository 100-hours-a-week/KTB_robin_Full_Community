package ktb3.fullstack.week4.service.posts;

import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.likes.Like;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostDeleteFacade {

    private final ErrorCheckServiceImpl errorCheckService;

    // 상위 트랜잭션 내에서만 실행
    @Transactional(propagation = Propagation.MANDATORY)
    public void deletePost(long postId) {
        Post post = errorCheckService.checkCanNotFoundPost(postId);

        // 게시글 이미지 Soft Delete
        for (PostImage postImage : post.getPostImages()) {
            if (!postImage.isDeleted()) {
                postImage.deleteEntity();
            }
        }

        // 게시글에 달린 댓글 Soft Delete
        for (Comment comment : post.getComments()) {
            if (!comment.isDeleted()) {
                comment.deleteEntity();
            }
        }

        // 게시글에 달린 좋아요 Soft Delete
        for (Like like : post.getLikes()) {
            if (!like.isDeleted()) {
                like.deleteEntity();
            }
        }

        // 게시글 Soft Delete
        if (!post.isDeleted()) {
            post.deleteEntity();
        }
        // 조회수(PostView)는 보존 정책 유지
    }
}