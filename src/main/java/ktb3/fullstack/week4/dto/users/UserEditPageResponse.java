package ktb3.fullstack.week4.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class UserEditPageResponse {
    @NotBlank
    private String email;
    @NotBlank
    private String nickname;
}
