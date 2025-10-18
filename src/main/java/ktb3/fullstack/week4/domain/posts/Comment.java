package ktb3.fullstack.week4.domain.posts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Comment {
    private long id;
    private long postId;
    private long authorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
