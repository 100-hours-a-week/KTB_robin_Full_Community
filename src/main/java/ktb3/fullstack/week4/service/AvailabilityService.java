package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
    private final UserRepository userRepository;

    public void checkEmailAvailability(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new ApiException(UserError.EXISTING_EMAIL);
        }
    }

    public void checkNicknameAvailability(String nickname) {
        if(userRepository.existsByNickname(nickname)) {
            throw new ApiException(UserError.EXISTING_NICKNAME);
        }
    }
}
