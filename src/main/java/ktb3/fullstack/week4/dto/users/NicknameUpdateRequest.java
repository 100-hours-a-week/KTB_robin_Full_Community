package ktb3.fullstack.week4.dto.users;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class NicknameUpdateRequest {
    @NotBlank
    private String newNickname;
}
