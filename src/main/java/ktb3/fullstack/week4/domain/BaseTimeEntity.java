package ktb3.fullstack.week4.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@SuperBuilder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "DATETIME(6)", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at", columnDefinition = "DATETIME(6)", nullable = false)
    private LocalDateTime modifiedAt;

    public void renewModifiedAt(LocalDateTime now) {
        this.modifiedAt = now;
    }
}
