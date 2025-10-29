package ktb3.fullstack.week4.domain.posts;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import ktb3.fullstack.week4.domain.SoftDeletetionEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends SoftDeletetionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NonNull
    private Long authorId; // author가 이름을 변경할 수 있기 때문에, authorId(userId) 토큰에서 꺼내쓸 예정

    @NonNull
    private String title;

    @NonNull
    private String content;

    private LocalDateTime deletedAt;

    private boolean isDeleted;

}
