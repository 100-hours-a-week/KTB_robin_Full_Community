package ktb3.fullstack.week4.api.auth;

import ktb3.fullstack.week4.config.swagger.annotation.CommonErrorResponses;
import ktb3.fullstack.week4.dto.auth.LoginResponse;
import ktb3.fullstack.week4.dto.auth.LoginRequest;
import ktb3.fullstack.week4.dto.auth.RefreshResponse;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.service.auth.AuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CommonErrorResponses
public class AuthController implements AuthApi {


    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest dto, HttpServletResponse response) {
        LoginResponse body = authService.login(dto, response);
        return ApiResponse.ok(body, "login_success");
    }

    @Override
    @PostMapping("/refresh")
    public ApiResponse<RefreshResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        RefreshResponse body = authService.refresh(request, response);
        return ApiResponse.ok(body, "access_token_refreshed");
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ApiResponse.ok("logout_success");
    }
}
