package ktb3.fullstack.week4.api.user;

import jakarta.validation.Valid;
import ktb3.fullstack.week4.Security.context.SecurityUser;
import ktb3.fullstack.week4.config.swagger.annotation.AccessTokenExpireResponse;
import ktb3.fullstack.week4.config.swagger.annotation.CommonErrorResponses;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.users.*;
import ktb3.fullstack.week4.service.availabilities.AvailabilityService;
import ktb3.fullstack.week4.service.users.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> register(
            @Valid @RequestPart JoinRequest dto,
            @RequestPart MultipartFile image) {
        availabilityService.checkRegisterAvailability(dto, image);
        userService.register(dto, image);
        return ApiResponse.ok("register_success");
    }

    // 회원정보 수정 페이지 진입 api
    @Override
    @AccessTokenExpireResponse
    @GetMapping("/me")
    public ApiResponse<UserEditPageResponse> getUserInfoForEditPage(
            @AuthenticationPrincipal SecurityUser user) {
            UserEditPageResponse result = userService.getUserInfoForEditPage(user.getId());
        return ApiResponse.ok(result, "userinfo_fetch_success");
    }

    @Override
    @AccessTokenExpireResponse
    @DeleteMapping("/me")
    public ApiResponse<Void> withdrawMemberShip(
            @AuthenticationPrincipal SecurityUser user) {
        userService.withdrawMemberShip(user.getId());
        return ApiResponse.ok("membership_withdraw_success");
    }

    @Override
    @AccessTokenExpireResponse
    @PatchMapping("/me/nickname") //회원정보 수정 - 닉네임
    public ApiResponse<NicknameUpdateResponse> changeNickname(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody NicknameUpdateRequest dto) {
        availabilityService.checkNewNicknameAvailability(dto);
        NicknameUpdateResponse result = userService.changeNickname(user.getId(), dto);
        return ApiResponse.ok(result, "nickname_edit_success");
    }

    @Override
    @AccessTokenExpireResponse
    @PatchMapping("/me/password") //회원정보 수정 - 비밀번호
    public ApiResponse<PasswordUpdateRequest> changePassword(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody PasswordUpdateRequest dto) {
        userService.changePassword(user.getId(), dto);
        return ApiResponse.ok("password_edit_success");
    }

    @Override
    @AccessTokenExpireResponse
    @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProfileImageUrlResponse> registerNewProfileImage(
            @RequestPart(value = "profile_image") MultipartFile newProfileImage,
            @AuthenticationPrincipal SecurityUser user) {
        // 생성된 url이 담겨 나간다
        ProfileImageUrlResponse reponse = new ProfileImageUrlResponse(
                userService.changeProfileImage(user.getId(), newProfileImage)
        );
        return ApiResponse.ok(reponse, "profile_image_upload_success");
    }

    @Override
    @AccessTokenExpireResponse
    @DeleteMapping("/me/profile-image")
    public ApiResponse<ProfileImageUrlResponse> removeProfileImage(
            @AuthenticationPrincipal SecurityUser user) {
        // 삭제한 이미지의 url이 담겨 나간다 (만약 클라이언트에서 url을 캐싱하고 있었다면 삭제 가능)
        ProfileImageUrlResponse reponse =
                new ProfileImageUrlResponse(userService.deleteProfileImage(user.getId()));
        return ApiResponse.ok(reponse, "profile_image_delete_success");
    }
}
