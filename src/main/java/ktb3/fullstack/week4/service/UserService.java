package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.image.ImageProcessor;
import ktb3.fullstack.week4.common.security.PasswordHasher;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateResponse;
import ktb3.fullstack.week4.dto.users.PasswordUpdateRequest;
import ktb3.fullstack.week4.repository.UserRepository;
import ktb3.fullstack.week4.store.images.ProfileImageStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final ImageProcessor imageProcessor;
    private final ProfileImageStore profileImageStore;

    public void register(JoinRequest dto) {
        String hashedPassword = passwordHasher.hash(dto.getPassword());
        /*
        * OCP 관점)
        *   문제점 : 생성자의 매개변수 순서가 바뀐다면 Service 코드도 수정 되어야함.
        *   개선 : 빌더 패턴을 사용하여 멤버 이름으로 명시적 초기화
        *       -> 이제 UserService 는 User 생성자의 매개변수 순서를 몰라도 된다
        * */
        User user = User.builder()
                .id(0L)
                .email(dto.getEmail())
                .hashedPassword(hashedPassword)
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl())
                .build();

        userRepository.save(user);
    }

    // 닉네임 변경: 존재 확인 → 저장소 갱신
    public NicknameUpdateResponse changeNickname(long userId, NicknameUpdateRequest dto) {
        User user = checkCanNotFoundUser(userId);

        String newNickname = dto.getNewNickname();
        String oldNickname = user.getNickname();
        user.changeNickName(newNickname);
        userRepository.updateNickname(user, oldNickname);
        return new NicknameUpdateResponse(newNickname);
    }

    // 비밀번호 변경: 존재 확인 → 해시 적용 → 저장(향후 Store/Repository 메서드 추가 시 연결)
    public void changePassword(long userId, PasswordUpdateRequest newPassword) {
        User user = checkCanNotFoundUser(userId);
        String hashedPassword = passwordHasher.hash(newPassword.getNewPassword());
        user.changePassword(hashedPassword);
        userRepository.updatePassword(user);
    }

    // 프로필 이미지 변경: 존재 확인 후 저장(향후 Store/Repository 메서드 추가 시 연결)
    public String changeProfileImage(long userId, MultipartFile newProfileImage) {
        User user = checkCanNotFoundUser(userId);
        /*
         이미지 저장할 외부 저장소 도입하면
         실제 이미지 저장 -> 외부 저장소
         이미지 URL 저장 -> DB (현재는 인메모리) 구조로 변경할 수 있음.
        */
        byte[] imageByte = imageProcessor.toByteStream(newProfileImage);
        String profileImageUrl = imageProcessor.makeRandomImageUrl();
        profileImageStore.uploadImage(profileImageUrl, imageByte);

        user.changeProfileImage(profileImageUrl);
        userRepository.updateProfileImage(user);
        return profileImageUrl;
    }

    // 프로필 이미지 삭제
    public String deleteProfileImage(long userId) {
        User user = checkCanNotFoundUser(userId);
        String existingProfileImageUrl = user.getProfileImageUrl();
        user.changeProfileImage(null);
        profileImageStore.deleteImage(existingProfileImageUrl);
        userRepository.deleteProfileImage(user);
        return existingProfileImageUrl;
    }

    // 회원 탈퇴 : 사용자 삭제
    public void withdrawMemberShip(long userId) {
        checkCanNotFoundUser(userId);
        userRepository.deleteById(userId);
    }

    private User checkCanNotFoundUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        return user;
    }
}
