package ktb3.fullstack.week4.dto.posts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostDetailResponse implements Serializable {

    @JsonProperty("post")
    private PostInfo post;

    private boolean isLiked;

    private boolean isOwner;

    @Getter
    @AllArgsConstructor
    public static class PostInfo implements Serializable {
        private long id;
        private String title;
        private long likeCount;
        private long commentCount;
        private long viewCount;
        private String author;
        private String authorProfileImageUrl;

        @JsonProperty("modified_at")
        private LocalDateTime modifiedAt;

        @JsonProperty("primary_image_url")
        private String primaryImageUrl;

        @JsonProperty("rest_image_urls")
        private List<String> restImageUrls;

        @JsonProperty("content")
        private String content;
    }
}
