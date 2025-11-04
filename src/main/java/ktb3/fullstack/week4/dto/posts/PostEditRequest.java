package ktb3.fullstack.week4.dto.posts;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostEditRequest {
    @NotBlank
    String title;
    @NotBlank
    String content;
}
