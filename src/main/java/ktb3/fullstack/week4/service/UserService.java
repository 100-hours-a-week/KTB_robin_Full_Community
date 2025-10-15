package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateResponse;
import ktb3.fullstack.week4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    // 회원 가입: 비즈니스 검증(중복) → 저장
    public void register(JoinRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new ApiException(UserError.EXISTING_EMAIL);
        }
        if (userRepository.existsByNickname(dto.getNickname())) {
            throw new ApiException(UserError.EXISTING_NICKNAME);
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        // TODO: 비밀번호 해시는 이 단계에서 적용(예: BCrypt) 후 저장
        // user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname());
        user.setProfileImageUrl(dto.getProfileImageUrl());
        userRepository.save(user);
    }

    // 닉네임 변경: 존재 확인 → 검증 → 저장소 갱신
    public NicknameUpdateResponse changeNickname(long userId, NicknameUpdateRequest dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));

        String newNickname = dto.getNewNickname();
        String oldNickname = user.getNickname();
        if (newNickname.equals(oldNickname)
                || userRepository.existsByNickname(newNickname)) {
            throw new ApiException(UserError.EXISTING_NICKNAME);
        }
        user.changeNickName(newNickname);
        userRepository.updateNickname(user, oldNickname);
        return new NicknameUpdateResponse(newNickname);
    }

    // 비밀번호 변경: 존재 확인 → 해시 적용 → 저장(향후 Store/Repository 메서드 추가 시 연결)
    public void changePassword(long userId, String rawNewPassword) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        // TODO: passwordEncoder.encode(rawNewPassword) 등 해시화 후 저장소 반영 메서드 호출
        // 예: userRepository.updatePassword(userId, hashedPassword);
    }

    // 프로필 이미지 변경: 존재 확인 후 저장(향후 Store/Repository 메서드 추가 시 연결)
    public void changeProfileImage(long userId, String newProfileImageUrl) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        // TODO: userRepository.updateImage(userId, newProfileImageUrl);
    }
}
