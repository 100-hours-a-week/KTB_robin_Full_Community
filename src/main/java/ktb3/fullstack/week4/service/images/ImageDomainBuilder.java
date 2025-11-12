package ktb3.fullstack.week4.service.images;

import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import ktb3.fullstack.week4.domain.posts.Post;
import ktb3.fullstack.week4.domain.users.User;
import org.springframework.stereotype.Component;

@Component
public class ImageDomainBuilder {

    public ProfileImage buildProfileImage(User user, String profileImageUrl) {
        ProfileImage image = ProfileImage.builder()
                .imageUrl(profileImageUrl)
                .isPrimary(true)
                .displayOrder(1)
                .build();

        image.linkUser(user);
        return image;
    }

    // 한 장 등록
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
