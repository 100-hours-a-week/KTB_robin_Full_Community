package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.security.PasswordHasher;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateResponse;
import ktb3.fullstack.week4.dto.users.PasswordUpdateRequest;
import ktb3.fullstack.week4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public void register(JoinRequest dto) {
        // 이메일, 닉네임 가용성 체크는 UserController 가 AvailabilityService 에 위임
        User user = new User();
        user.setEmail(dto.getEmail());
        String hashedPassword = passwordHasher.hash(dto.getPassword());
        user.setPassword(hashedPassword);
        user.setNickname(dto.getNickname());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        userRepository.save(user);
    }

    // 닉네임 변경: 존재 확인 → 저장소 갱신
    public NicknameUpdateResponse changeNickname(long userId, NicknameUpdateRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));

        String newNickname = dto.getNewNickname();
        String oldNickname = user.getNickname();
        // 닉네임 가용성 체크는 UserController 가 AvailabilityService 에 위임
        user.changeNickName(newNickname);
        userRepository.updateNickname(user, oldNickname);
        return new NicknameUpdateResponse(newNickname);
    }

    // 비밀번호 변경: 존재 확인 → 해시 적용 → 저장(향후 Store/Repository 메서드 추가 시 연결)
    public void changePassword(long userId, PasswordUpdateRequest newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        // TODO: passwordEncoder.encode(newPassword) 등 해시화 후 저장소 반영 메서드 호출
        String hashedPassword = passwordHasher.hash(newPassword.getNewPassword());
        user.changePassword(hashedPassword);
        userRepository.updatePassword(user);
    }

    // 프로필 이미지 변경: 존재 확인 후 저장(향후 Store/Repository 메서드 추가 시 연결)
    public void changeProfileImage(long userId, String newProfileImageUrl) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        // TODO: userRepository.updateImage(userId, newProfileImageUrl);
    }
}
