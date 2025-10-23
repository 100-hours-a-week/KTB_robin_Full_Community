package ktb3.fullstack.week4.service.availability;

import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.dto.users.NicknameUpdateRequest;

public interface AvailabilityService {
    void checkRegisterAvailability(JoinRequest dto);
    void checkNewNicknameAvailability(NicknameUpdateRequest dto);
}
