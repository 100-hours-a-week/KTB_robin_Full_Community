package ktb3.fullstack.week4.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class SoftDeletetionEntity extends BaseTimeEntity {

    @Column(name = "deleted")
    private boolean deleted;
}
