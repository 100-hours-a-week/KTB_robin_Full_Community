package ktb3.fullstack.week4.domain.images;

import jakarta.persistence.Column;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import lombok.Getter;

@Getter
public abstract class Image extends SoftDeletetionEntity {

    @Column(name = "image_url")
    private String imageUrl;
}
