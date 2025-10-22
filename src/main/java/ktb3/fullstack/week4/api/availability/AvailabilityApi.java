package ktb3.fullstack.week4.api.availability;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[개인정보 가용성 검사 API]")
public interface AvailabilityApi {

    @Operation(summary = "이메일 가용성 검사", description = "등록 가능한 이메일인지 검사합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "사용 가능한 이메일입니다.", value = """
                            {
                                "message" : "valid_email",
                                "data" : null
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "이미 존재하는 이메일입니다.", value = """
                            {
                                "message" : "existing_email",
                                "data" : null
                            }
                            """),
                    })
            )
    })
    ApiResponse<Void> checkEmailAvailability(@RequestParam(name = "value") String email);

    @Operation(summary = "닉네임 가용성 검사", description = "등록 가능한 닉네임인지 검사합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "사용 가능한 닉네임입니다.", value = """
                            {
                                "message" : "valid_nickname",
                                "data" : null
                            }
                            """),
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "이미 존재하는 닉네임입니다.", value = """
                            {
                                "message" : "existing_nickname",
                                "data" : null
                            }
                            """),
                    })
            )
    })
    ApiResponse<Void> checkNicknameAvailability(@RequestParam(name = "value") String nickname);
}
