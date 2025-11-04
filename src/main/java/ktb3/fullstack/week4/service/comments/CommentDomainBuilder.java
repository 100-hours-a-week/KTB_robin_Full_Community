package ktb3.fullstack.week4.service.comments;

import ktb3.fullstack.week4.domain.comments.Comment;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import org.springframework.stereotype.Component;

@Component
public class CommentDomainBuilder {

    public Comment buildComment(User user, Post post, String content) {
        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .post(post)
                .build();

        comment.linkPost(post);
        return comment;
    }
}
