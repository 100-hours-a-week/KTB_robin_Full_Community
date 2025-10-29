package ktb3.fullstack.week4.domain.images;

import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import lombok.Getter;

@Getter
public abstract class Image extends SoftDeletetionEntity {

    private String imageUrl;
}
