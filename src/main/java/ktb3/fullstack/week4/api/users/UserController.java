package ktb3.fullstack.week4.api.users;

import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateResponse;
import ktb3.fullstack.week4.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ApiResponse<Void> register(@Valid @RequestBody JoinRequest dto) {
        userService.register(dto);
        return ApiResponse.ok("register_success");
    }

    @PatchMapping("/me/nickname")
    public ApiResponse<NicknameUpdateResponse> changeNickName(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestBody NicknameUpdateRequest dto) {
        NicknameUpdateResponse result = userService.changeNickname(userId, dto);
        return ApiResponse.ok(result, "nickname_edit_success");
    }
}
