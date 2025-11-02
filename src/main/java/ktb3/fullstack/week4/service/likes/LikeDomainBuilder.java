package ktb3.fullstack.week4.service.likes;

import ktb3.fullstack.week4.domain.likes.Like;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import org.springframework.stereotype.Component;

@Component
public class LikeDomainBuilder {

    public Like buildLike(Post post, User user) {
        return Like.builder()
                .isLiked(true)
                .user(user)
                .post(post)
                .build();
    }
}
