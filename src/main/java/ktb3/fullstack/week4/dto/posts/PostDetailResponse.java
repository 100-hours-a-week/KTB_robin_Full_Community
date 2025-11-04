package ktb3.fullstack.week4.dto.posts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostDetailResponse {

    @JsonProperty("post")
    private PostInfo post;

    @JsonProperty("is_liked")
    private boolean isLiked;

    @JsonProperty("comments")
    private List<CommentInfo> comments;

    @Getter
    @AllArgsConstructor
    public static class PostInfo {
        private long id;
        private String title;
        private long likes;
        private long comments;
        private long views;
        private String author;

        @JsonProperty("modified_at")
        private LocalDateTime modifiedAt;

        @JsonProperty("primary_image_url")
        private String primaryImageUrl;

        @JsonProperty("rest_image_urls")
        private List<String> restImageUrls;

        @JsonProperty("content")
        private String content;
    }

    @Getter
    @AllArgsConstructor
    public static class CommentInfo {
        private long id;
        private String author;
        private String content;

        @JsonProperty("modified_at")
        private LocalDateTime modifiedAt;
    }
}
