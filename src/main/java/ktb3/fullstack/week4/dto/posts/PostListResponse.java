package ktb3.fullstack.week4.dto.posts;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostListResponse {
    @Nullable
    @JsonProperty("posts")
    private List<PostBriefInfo> posts;

    @JsonProperty("next_cursor")
    private int nextCursor;

    @JsonProperty("has_next")
    private boolean hasNext;

    @Getter
    @AllArgsConstructor
    public static class PostBriefInfo {
        private long id;
        private String title;
        private long likeCount;
        private long commentCount;
        private long viewCount;
        private String author;
        private String authorProfileImageUrl;

        @JsonProperty("modified_at")
        private LocalDateTime modifiedAt;
    }
}
