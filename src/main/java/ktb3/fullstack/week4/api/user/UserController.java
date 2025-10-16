package ktb3.fullstack.week4.api.user;

import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateResponse;
import ktb3.fullstack.week4.dto.users.PasswordUpdateRequest;
import ktb3.fullstack.week4.service.AvailabilityService;
import ktb3.fullstack.week4.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AvailabilityService availabilityService;

    @PostMapping //회원가입
    public ApiResponse<Void> register(@Valid @RequestBody JoinRequest dto) {
        availabilityService.checkEmailAvailability(dto.getEmail());
        availabilityService.checkNicknameAvailability(dto.getNickname());
        userService.register(dto);
        return ApiResponse.ok("register_success");
    }

    @PatchMapping("/me/nickname") //회원정보 수정 - 닉네임
    public ApiResponse<NicknameUpdateResponse> changeNickName(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestBody NicknameUpdateRequest dto) {
        availabilityService.checkNicknameAvailability(dto.getNewNickname());
        NicknameUpdateResponse result = userService.changeNickname(userId, dto);
        return ApiResponse.ok(result, "nickname_edit_success");
    }

    @PatchMapping("/me/password") //회원정보 수정 - 비밀번호
    public ApiResponse<PasswordUpdateRequest> changeNickName(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestBody PasswordUpdateRequest dto) {
        userService.changePassword(userId, dto);
        return ApiResponse.ok("password_edit_success");
    }
}
