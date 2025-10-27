package ktb3.fullstack.week4.api.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.users.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "[사용자 API]")
public interface UserApi {

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록합니다. 이메일/닉네임 가용성은 사전 검사합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "회원 가입 성공", value = """
                            {
                                "message" : "register_success",
                                "data" : null
                            }
                            """),
                    })
            )
    })
    ApiResponse<Void> register(@Valid @RequestBody JoinRequest dto);

    @Operation(summary = "회원 정보 조회(간략)", description = "회원의 이메일과 닉네임 정보를 전달합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "회원 정보 조회(간략) 성공", value = """
                            {
                                "message" : "userinfo_request_success",
                                "data" : {
                                    "email": "robin123@kakao.com",
                                    "nickname": "robin.choi"
                                }
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "해당 사용자를 찾을 수 없습니다.", value = """
                            {
                                "message" : "cannot_found_user",
                                "data" : null
                            }
                            """),
                    })
            ),
    })
    ApiResponse<UserEditPageResponse> getUserInfoForEditPage(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId);

    @Operation(summary = "회원 탈퇴", description = "현재 사용자를 탈퇴 처리합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "회원 탈퇴 성공", value = """
                            {
                                "message" : "membership_withdraw_success",
                                "data" : null
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "해당 사용자를 찾을 수 없습니다.", value = """
                            {
                                "message" : "cannot_found_user",
                                "data" : null
                            }
                            """),
                    })
            ),
    })
    ApiResponse<Void> withdrawMemberShip(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId
    );

    @Operation(summary = "닉네임 변경", description = "현재 사용자의 닉네임을 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "닉네임 변경 성공", value = """
                            {
                                "message" : "nickname_edit_success",
                                "data" : {
                                    "newNickname": "robin.choi"
                                }
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "해당 사용자를 찾을 수 없습니다.", value = """
                            {
                                "message" : "cannot_found_user",
                                "data" : null
                            }
                            """),
                    })
            ),
    })
    ApiResponse<NicknameUpdateResponse> changeNickname(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestBody NicknameUpdateRequest dto
    );


    @Operation(summary = "비밀번호 변경", description = "현재 사용자의 비밀번호를 변경합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "비밀번호 변경 성공", value = """
                            {
                                "message" : "password_edit_success",
                                "data" : null
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "해당 사용자를 찾을 수 없습니다.", value = """
                            {
                                "message" : "cannot_found_user",
                                "data" : null
                            }
                            """),
                    })
            ),
    })
    ApiResponse<PasswordUpdateRequest> changePassword(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestBody PasswordUpdateRequest dto
    );

    @Operation(summary = "프로필 이미지 업로드", description = "새 프로필 이미지를 업로드하거나 기존 이미지를 교체합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "프로필 이미지 업로드 성공", value = """
                            {
                                "message" : "profile_image_upload_success",
                                "data" : {
                                    "image_url": "http://www.example.com/images/my_image.jpg"
                                 }
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "이미지 용량이 너무 큽니다.", value = """
                            {
                                "message" : "image_size_too_big",
                                "data" : null
                            }
                            """),
                            @ExampleObject(name = "첨부한 파일의 타입은 지원하지 않습니다.", value = """
                            {
                                "message" : "invalid_file_type",
                                "data" : null
                            }
                            """)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "해당 사용자를 찾을 수 없습니다.", value = """
                            {
                                "message" : "cannot_found_user",
                                "data" : null
                            }
                            """),
                    })
            ),
    })
    ApiResponse<ProfileImageUrlResponse> registerNewProfileImage(
            @Parameter(
                    name = "profile_image",
                    description = "프로필 이미지 파일",
                    content = @Content(mediaType = "multipart/form-data")
            )
            @RequestPart(value = "profile_image") MultipartFile newProfileImage,
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId
    );

    @Operation(summary = "프로필 이미지 삭제", description = "등록된 프로필 이미지를 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "프로필 이미지 삭제 성공", value = """
                            {
                                "message" : "profile_image_delete_success",
                                "data" : {
                                    "image_url": "http://www.example.com/images/my_image.jpg"
                                 }
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "해당 사용자를 찾을 수 없습니다.", value = """
                            {
                                "message" : "cannot_found_user",
                                "data" : null
                            }
                            """),
                    })
            ),
    })
    ApiResponse<ProfileImageUrlResponse> removeProfileImage(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId
    );
}
