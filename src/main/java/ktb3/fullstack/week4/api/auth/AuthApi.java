package ktb3.fullstack.week4.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import ktb3.fullstack.week4.dto.auth.LoginRequest;
import ktb3.fullstack.week4.dto.auth.LoginResponse;
import ktb3.fullstack.week4.dto.auth.RefreshResponse;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[인증 API]")
public interface AuthApi {

    @Operation(summary = "로그인", description = "로그인합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "로그인 성공", value = """
                            {
                                "message" : "login_success",
                                "data" : {
                                     "profile_image_url": 1a3e8a9f-9d95-41eb-b076-485b113e24db여세요.PNG"
                                     "access_token": "eyJhbGciOi ...",
                                     "token_type": "Bearer",
                                     "access_expired_in": 3600,\s
                                     "refresh_expired_in": 604800
                                 }
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "로그인 실패 - 올바르지 않은 이메일 혹은 비밀번호입니다.", value = """
                            {
                                "message" : "invalid_request",
                                "data" : null
                            }
                            """),
                    })
            )
    })
    ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest dto, HttpServletResponse response);

    @Operation(summary = "액세스 토큰 갱신", description = "새로운 액세스 토큰을 받아옵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "액세스 토큰 재발급 성공", value = """
                            {
                                "message" : "access_token_refreshed",
                                "data" : {
                                     "access_token": "eyJhbGciOi ...",
                                     "token_type": "Bearer",
                                     "access_expired_in": 3600  \s
                                 }
                            }
                            """),
                    })
            ),
            // 액세스 만료, 리프레시는 만료 응답은 공통으로 묶어서 재사용할만함
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "리프레시 토큰이 만료되었습니다. 재로그인 하세요", value = """
                            {
                                "message" : "refresh_token_expired",
                                "data" : null
                            }
                            """),
                    })
            )
    })
    ApiResponse<RefreshResponse> refresh(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "로그아웃", description = "로그아웃합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "로그아웃 성공", value = """
                            {
                                "message" : "logout_success",
                                "data" : null
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401", description = "refresh_token_expired",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "리프레시 토큰이 만료되었습니다.", value = """
                            {
                                "message" : "refresh_token_expired",
                                "data" : null
                            }
                            """),
                    })
            )
    })
    ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response);
}
