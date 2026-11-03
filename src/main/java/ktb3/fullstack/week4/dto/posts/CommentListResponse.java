package ktb3.fullstack.week4.dto.posts;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CommentListResponse {

    @Nullable
    @JsonProperty("comments")
    private List<CommentInfo> comments;

    @JsonProperty("next_cursor_id")
    private Long nextCursorId;

    @JsonProperty("has_next")
    private boolean hasNext;

    @Getter
    @AllArgsConstructor
    public static class CommentInfo {
        private long id;
        private String author;
        private String content;
        private String authorProfileImageUrl;

        @JsonProperty("modified_at")
        private LocalDateTime modifiedAt;
    }
}
