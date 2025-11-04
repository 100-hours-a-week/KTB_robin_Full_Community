package ktb3.fullstack.week4.service.posts;

import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.posts.PostUploadRequeset;
import org.springframework.stereotype.Component;

@Component
public class PostDomainBuilder {

    public Post buildPost(PostUploadRequeset dto, User user) {
        return Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .postView(null)
                .user(user)
                .build();
    }

    public PostImage buildPostImage(Post post, String postImageUrl) {
        PostImage postImage = PostImage.builder()
            .post(post)
            .imageUrl(postImageUrl)
            .displayOrder(1)
            .isPrimary(true)
            .build();

        postImage.linkPost(post);
        return postImage;
    }
}
