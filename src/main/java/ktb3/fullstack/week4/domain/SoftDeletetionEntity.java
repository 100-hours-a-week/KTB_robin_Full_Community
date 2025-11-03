package ktb3.fullstack.week4.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@MappedSuperclass
@SuperBuilder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted = false") // 모든 조회 쿼리에 자동으로 "where deleted = false" 추가
public abstract class SoftDeletetionEntity extends BaseTimeEntity {

    @Column(name = "deleted")
    protected boolean deleted;

    public void deleteEntity() {
        this.deleted = true;
    }
}
