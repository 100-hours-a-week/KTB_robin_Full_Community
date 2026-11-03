package ktb3.fullstack.week4.dto.users;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateRequest {

    // 1. (?=.*[a-z]): 최소 1개의 소문자 포함
    // 2. (?=.*[A-Z]): 최소 1개의 대문자 포함
    // 3. (?=.*[!@#$%^&*]): 최소 1개의 특수문자 포함 (허용할 특수문자 범위 지정)
    // 4. .{8,20}: 전체 길이 8~20자
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$",
            message = "비밀번호는 8~20자여야 하며, 대문자, 소문자, 특수문자를 각각 최소 1개 이상 포함해야 합니다."
    )
    private String newPassword;
}
