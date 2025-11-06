package ktb3.fullstack.week4.service.availabilities;

import ktb3.fullstack.week4.common.error.codes.FileError;
import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;
import ktb3.fullstack.week4.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {
    private final UserRepository userRepository;

    // 회원가입 시 이메일과 닉네임의 가용성 검사
    @Override
    public void checkRegisterAvailability(JoinRequest dto, MultipartFile image) {
        checkEmailAvailability(dto.getEmail());
        checkNicknameAvailability(dto.getNickname());
        checkImageAvailability(image);
    }

    // 닉네임 변경 시 변경 희망 닉네임의 가용성 검사
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

    public void checkImageAvailability(MultipartFile image) {
        final long MAX_SIZE = 10L * 1024 * 1024;
        if(image == null) {
            throw new ApiException(FileError.IMAGE_NOT_FOUND);
        }
        if(image.getSize() > MAX_SIZE) {
            throw new ApiException(FileError.IMAGE_SIZE_TOO_BIG);
        }
        // JPEG, PNG 만 받도록 처리 -> invalid file type
        String imageType = image.getContentType();
        if(!(imageType.equals("image/jpeg") || imageType.equals("image/png"))) {
            throw new ApiException(FileError.INVALID_FILE_TYPE);
        }
    }
}
