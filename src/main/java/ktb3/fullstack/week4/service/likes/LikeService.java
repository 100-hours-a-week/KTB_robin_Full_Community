package ktb3.fullstack.week4.service.likes;

import ktb3.fullstack.week4.domain.likes.Like;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.repository.likes.LikeRepository;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LikeService {

    private final LikeDomainBuilder likeDomainBuilder;

    private final LikeRepository likeRepository;

    private final ErrorCheckServiceImpl errorCheckService;

    // 게시글 좋아요
    @Transactional
    public void likePost(long userId, long postId) {
        User user = errorCheckService.checkCanNotFoundUser(userId);
        Post post = errorCheckService.checkCanNotFoundPost(postId);

        Like like = likeRepository.findByPostIdAndUserId(postId, userId);
        if(like == null) {
            Like newLike = likeDomainBuilder.buildLike(post, user);
            likeRepository.save(newLike);
            return;
        }
        like.flipIsLiked();
    }

    // 게시글 좋아요 취소
    @Transactional
    public void unlikePost(long userId, long postId) {
        errorCheckService.checkCanNotFoundUser(userId);
        errorCheckService.checkCanNotFoundPost(postId);
        Like like = errorCheckService.checkCanNotFoundLike(postId, userId);
        like.flipIsLiked();
    }
}
