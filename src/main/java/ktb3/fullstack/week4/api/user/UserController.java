package ktb3.fullstack.week4.api.user;

import jakarta.validation.Valid;
import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.config.swagger.annotation.AccessTokenExpireResponse;
import ktb3.fullstack.week4.config.swagger.annotation.CommonErrorResponses;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.users.*;
import ktb3.fullstack.week4.service.availability.AvailabilityService;
import ktb3.fullstack.week4.service.availability.AvailabilityServiceImpl;
import ktb3.fullstack.week4.service.UserService;
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

    /*
     * 현재는 검증 로직이 2개가 있지만, 이후 password 검증이 추가된다고 하면,
     * Service에 생긴 새로운 메소드를 컨트롤러에도 직접 붙여야 한다.
     * 반대 상황으로 검증 로직이 하나 제거되는 상황에도 마찬가지다.
     *
     * 컨트롤러는 checkRegisterAvailability(dto) 만 수행하도록 수정한다.
     * availServiceImpl 는 이제 AvailabilityService 를 구현하는 구현체 인스턴스로,
     * JoinRequest의 구성 자체가 변하지 않는한, AvailServiceImpl 에서만 가용성 검사 로직을 관리한다.
     *
     * 이제 회원가입에 필요한 가용성 검사가 더 늘어나거나 줄어든다고 하더라도,
     * 코드는 변경은 오직 AvailabilityServiceImpl 에서만 일어난다.
     * */
    @Override
    @PostMapping
    public ApiResponse<Void> register(@Valid @RequestBody JoinRequest dto) {
        availabilityService.checkRegisterAvailability(dto);
        userService.register(dto);
        return ApiResponse.ok("register_success");
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
    @DeleteMapping("/me")
    public ApiResponse<Void> withdrawMemberShip(
            @RequestAttribute(JwtAuthInterceptor.USER_ID) long userId) {
        userService.withdrawMemberShip(userId);
        return ApiResponse.ok("membership_withdraw_success");
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
