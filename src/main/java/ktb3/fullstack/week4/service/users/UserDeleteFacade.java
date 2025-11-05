package ktb3.fullstack.week4.service.users;

import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import ktb3.fullstack.week4.domain.likes.Like;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.repository.auth.RefreshTokenRepository;
import ktb3.fullstack.week4.repository.comments.CommentRepository;
import ktb3.fullstack.week4.repository.likes.LikeRepository;
import ktb3.fullstack.week4.repository.posts.PostRepository;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
import ktb3.fullstack.week4.service.posts.PostDeleteFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDeleteFacade {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final ErrorCheckServiceImpl errorCheckService;
    private final PostDeleteFacade postDeleteFacade;

    // 반드시 상위 트랜잭션 안에서만 실행되도록 강제(호출 측에서 @Transactional)
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteUser(long userId) {
        User user = errorCheckService.checkCanNotFoundUser(userId);

        // 프로필 이미지 삭제 처리
        List<ProfileImage> profileImages = user.getProfileImages();
        for (ProfileImage profileImage : profileImages) {
            if(!profileImage.isDeleted()) {
                profileImage.deleteEntity();
            }
        }

        // 게시글
        List<Post> postListOfUser = postRepository.findAllByUserId(userId);
        for (Post post : postListOfUser) {
            // 게시글 이미지 삭제 처리
            postDeleteFacade.deletePost(post.getId());
        }
        // 사용자가 작성한 모든 댓글
        List<Comment> comments = commentRepository.findAllByUserId(userId);
        for (Comment comment : comments) {
            if(!comment.isDeleted()) {
                comment.deleteEntity();
            }
        }
        // 사용자가 남긴 모든 좋아요
        List<Like> likes = likeRepository.findAllByUserId(userId);
        for (Like like : likes) {
            if(!like.isDeleted()) {
                like.deleteEntity();
            }
        }
        // 리프레시 토큰 (hard)
        refreshTokenRepository.deleteByUserId(userId);
        // 사용자 본인
        user.deleteUser(); // deleted = true + deletedAt 최신화

        // 조회수는 남겨 놓기. 만약 일정기간 이후 게시글 완전삭제 된다면, 그 때 hard delete
    }
}
