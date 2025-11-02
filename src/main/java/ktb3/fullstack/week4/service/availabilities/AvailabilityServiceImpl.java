package ktb3.fullstack.week4.service.availabilities;

import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;
import ktb3.fullstack.week4.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {
    private final UserRepository userRepository;

    @Override
    public void checkRegisterAvailability(JoinRequest dto) {
        checkEmailAvailability(dto.getEmail());
        checkNicknameAvailability(dto.getNickname());
    }

    @Override
    public void checkNewNicknameAvailability(NicknameUpdateRequest dto) {
        checkNicknameAvailability(dto.getNewNickname());
    }

    @Override
    public void checkEmailAvailability(String email) {
        if(userRepository.existsByEmail(email)) {
            throw new ApiException(UserError.EXISTING_EMAIL);
        }
    }

    @Override
    public void checkNicknameAvailability(String nickname) {
        if(userRepository.existsByNickname(nickname)) {
            throw new ApiException(UserError.EXISTING_NICKNAME);
        }
    }
}
