package ktb3.fullstack.week4.api.user;

import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.config.swagger.annotation.AccessTokenExpireResponse;
import ktb3.fullstack.week4.config.swagger.annotation.CommonErrorResponses;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.users.*;
import ktb3.fullstack.week4.service.availabilities.AvailabilityService;
import ktb3.fullstack.week4.service.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CommonErrorResponses
public class UserController implements UserApi {

    private final UserService userService;
    private final AvailabilityService availabilityService;

    @Override
    @PostMapping
    public ApiResponse<Void> register(@Valid @RequestBody JoinRequest dto) {
        availabilityService.checkRegisterAvailability(dto);
        userService.register(dto);
        return ApiResponse.ok("register_success");
    }

    // 회원정보 수정 페이지 진입 api
    @Override
    @AccessTokenExpireResponse
    @GetMapping("/me") //회원정보 수정 - 닉네임
    public ApiResponse<UserEditPageResponse> getUserInfoForEditPage(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId) {
            UserEditPageResponse result = userService.getUserInfoForEditPage(userId);
        return ApiResponse.ok(result, "nickname_edit_success");
    }

    @Override
    @AccessTokenExpireResponse
    @DeleteMapping("/me")
    public ApiResponse<Void> withdrawMemberShip(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId) {
        userService.withdrawMemberShip(userId);
        return ApiResponse.ok("membership_withdraw_success");
    }

    @Override
    @AccessTokenExpireResponse
    @PatchMapping("/me/nickname") //회원정보 수정 - 닉네임
    public ApiResponse<NicknameUpdateResponse> changeNickname(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestBody NicknameUpdateRequest dto) {
        availabilityService.checkNewNicknameAvailability(dto);
        NicknameUpdateResponse result = userService.changeNickname(userId, dto);
        return ApiResponse.ok(result, "nickname_edit_success");
    }

    @Override
    @AccessTokenExpireResponse
    @PatchMapping("/me/password") //회원정보 수정 - 비밀번호
    public ApiResponse<PasswordUpdateRequest> changePassword(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId,
            @Valid @RequestBody PasswordUpdateRequest dto) {
        userService.changePassword(userId, dto);
        return ApiResponse.ok("password_edit_success");
    }

    @Override
    @AccessTokenExpireResponse
    @PatchMapping("/me/profile-image")
    public ApiResponse<ProfileImageUrlResponse> registerNewProfileImage(
            @RequestPart(value = "profile_image") MultipartFile newProfileImage,
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId) {
        // 생성된 url이 담겨 나간다
        ProfileImageUrlResponse reponse = new ProfileImageUrlResponse(
                userService.changeProfileImage(userId, newProfileImage)
        );
        return ApiResponse.ok(reponse, "profile_image_upload_success");
    }

    @Override
    @AccessTokenExpireResponse
    @DeleteMapping("/me/profile-image")
    public ApiResponse<ProfileImageUrlResponse> removeProfileImage(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId) {
        // 삭제한 이미지의 url이 담겨 나간다 (만약 클라이언트에서 url을 캐싱하고 있었다면 삭제 가능)
        ProfileImageUrlResponse reponse =
                new ProfileImageUrlResponse(userService.deleteProfileImage(userId));
        return ApiResponse.ok(reponse, "profile_image_delete_success");
    }
}
