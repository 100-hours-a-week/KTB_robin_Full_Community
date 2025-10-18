package ktb3.fullstack.week4.domain.posts;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Post {
    private Long id;
    @NonNull
    private Long authorId; // author가 이름을 변경할 수 있기 때문에, authorId(userId) 토큰에서 꺼내쓸 예정
    @NonNull
    private String title;
    @NonNull
    private String content;
    @Nullable
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
