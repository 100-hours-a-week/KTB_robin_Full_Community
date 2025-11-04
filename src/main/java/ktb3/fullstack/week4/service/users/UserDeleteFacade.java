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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDeleteFacade {

    private final PostRepository postRepository; // postimage 는 더티체킹으로 함께 삭제

    private final CommentRepository commentRepository;

    private final LikeRepository likeRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final ErrorCheckServiceImpl errorCheckService;

     /*
     질문 1. 만약 아래 트랜잭션 어노테이션이 없다면, UserService.withdrawMemberShip 메소드에서 퍼사드의 deleteUser 메소드를 호출했을때,
     deleteUser 메소드는 withdrawMemberShip 메소드의 트랜잭션 안에서 실행되는게 아니야?
     */
     // 반드시 상위 트랜잭션 안에서만 실행되도록 강제(호출 측에서 @Transactional)
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteUser(long userId) {
        User user = errorCheckService.checkCanNotFoundUser(userId);

        // 프로필 이미지 삭제 처리
        List<ProfileImage> profileImages = user.getProfileImages();
        for (ProfileImage profileImage : profileImages) {
             /*
             질문 2. 이렇게 하면 deleted = true 로 바뀐 엔티티들의 modifiedAt 속성만 현재로 변경되니?
             위 질문은 Auditing 이런 상황에서 동작하는지에 대한 질문임.
             */
            if(!profileImage.isDeleted()) {
                profileImage.deleteEntity();
            }
        }

        /*
        질문 3: 아래의 코드들도 모두 정상적으로 더티체킹이 동작하여서 DB 에도 반영되니?
        */
        // 게시글
        List<Post> postListOfUser = postRepository.findAllByUserId(userId);
        for (Post post : postListOfUser) {
            // 게시글 이미지 삭제 처리
            for (PostImage postImage : post.getPostImages()) {
                if (!postImage.isDeleted()) {
                    postImage.deleteEntity();
                }
            }
            // 게시글에 달린 댓글 삭제 처리
            for (Comment comment : post.getComments()) {
                if (!comment.isDeleted()) {
                    comment.deleteEntity();
                }
            }
            // 게시글에 달린 좋아요 삭제 처리
            for (Like like : post.getLikes()) {
                if(!like.isDeleted()) {
                    like.deleteEntity();
                }
            }
            // 게시글 삭제 처리
            if (!post.isDeleted()) {
                post.deleteEntity();
            }
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
