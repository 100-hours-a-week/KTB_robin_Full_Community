package ktb3.fullstack.week4.domain.images;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@SuperBuilder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Image extends SoftDeletetionEntity {

    @Column(name = "image_url", unique = true)
    protected String imageUrl;

    @Column(name = "is_primary")
    protected boolean isPrimary;

    @Column(name = "display_order")
    protected int displayOrder;
}
