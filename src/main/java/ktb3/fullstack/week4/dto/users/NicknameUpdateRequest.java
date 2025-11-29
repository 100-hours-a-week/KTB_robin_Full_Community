package ktb3.fullstack.week4.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameUpdateRequest {
    @NotBlank
    private String newNickname;
}
