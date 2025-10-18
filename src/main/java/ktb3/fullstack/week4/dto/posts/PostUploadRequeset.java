package ktb3.fullstack.week4.dto.posts;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostUploadRequeset {
    @NotBlank
    String title;
    @NotBlank
    String content;
}
