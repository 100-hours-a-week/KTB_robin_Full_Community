package ktb3.fullstack.week4.api.availability;

import ktb3.fullstack.week4.config.swagger.annotation.CommonErrorResponses;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.service.availability.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/availability")
@RequiredArgsConstructor
@CommonErrorResponses
public class AvailabilityController implements AvailabilityApi {
    private final AvailabilityService availabilityService;

    @Override
    @GetMapping("/email")
    public ApiResponse<Void> checkEmailAvailability(@RequestParam(name = "value") String email) {
        availabilityService.checkEmailAvailability(email);
        return ApiResponse.ok("valid_email");
    }

    @Override
    @GetMapping("/nickname")
    public ApiResponse<Void> checkNicknameAvailability(@RequestParam(name = "value") String nickname) {
        availabilityService.checkNicknameAvailability(nickname);
        return ApiResponse.ok("valid_nickname");
    }
}
