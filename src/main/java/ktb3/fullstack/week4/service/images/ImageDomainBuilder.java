package ktb3.fullstack.week4.service.images;

import ktb3.fullstack.week4.domain.images.PostImage;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import org.springframework.stereotype.Component;

@Component
public class ImageDomainBuilder {

    public ProfileImage profileImageBuilder(String profileImageUrl) {
        return ProfileImage.builder()
                .imageUrl(profileImageUrl)
                .isPrimary(true)
                .displayOrder(1)
                .build();
    }

    public PostImage postImageBuilder(String postImageUrl, boolean isPrimary, int displayOrder) {
        return PostImage.builder()
                .imageUrl(postImageUrl)
                .isPrimary(isPrimary)
                .displayOrder(displayOrder)
                .build();
    }
}
