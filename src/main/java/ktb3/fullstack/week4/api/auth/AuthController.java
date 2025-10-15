package ktb3.fullstack.week4.api.auth;

import ktb3.fullstack.week4.dto.auth.LoginResponse;
import ktb3.fullstack.week4.dto.auth.LoginRequest;
import ktb3.fullstack.week4.dto.auth.RefreshResponse;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.service.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest dto, HttpServletResponse response) {
        LoginResponse body = authService.login(dto, response);
        return ApiResponse.ok(body, "login_success");
    }


    @PostMapping("/refresh")
    public ApiResponse<RefreshResponse> refresh(HttpServletRequest request) {
        RefreshResponse body = authService.refresh(request);
        return ApiResponse.ok(body, "access_token_refreshed");
    }


    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ApiResponse.ok("logout_success");
    }
}
