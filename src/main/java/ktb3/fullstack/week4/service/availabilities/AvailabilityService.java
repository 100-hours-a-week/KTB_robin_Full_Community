package ktb3.fullstack.week4.service.availabilities;

import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AvailabilityService {
    void checkRegisterAvailability(JoinRequest dto, MultipartFile image);
    void checkNewNicknameAvailability(NicknameUpdateRequest dto);
    void checkEmailAvailability(String eamil);
    void checkNicknameAvailability(String nickname);
}
