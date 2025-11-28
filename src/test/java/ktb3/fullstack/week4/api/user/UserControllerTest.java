package ktb3.fullstack.week4.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb3.fullstack.week4.Security.config.SecurityConfig;
import ktb3.fullstack.week4.Security.jwt.JwtAuthenticationFilter;
import ktb3.fullstack.week4.common.error.codes.FileError;
import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.dto.users.JoinRequest;
import ktb3.fullstack.week4.service.availabilities.AvailabilityService;
import ktb3.fullstack.week4.service.users.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
        }
)
@Import(UserControllerTest.TestSecurityConfig.class)
public class UserControllerTest {

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean // @Mock 으로 설정하면 에러 -> @WebMvcTest 는 실제로 컨트롤러와 관련된 빈을 등록하기 때문
    UserService userService;

    @MockitoBean
    AvailabilityService availabilityService;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    private MockMultipartFile createDtoFile() throws JsonProcessingException {

        JoinRequest joinRequest = JoinRequest.builder()
                .email("yongsu626@naver.com")
                .password("Yongsu626!")
                .nickname("robin123")
                .build();

        return new MockMultipartFile(
                "dto",
                "",
                "application/json",
                objectMapper.writeValueAsString(joinRequest).getBytes(StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile createImageFile() {
        return new MockMultipartFile(
                "image",
                "profile.jpeg",
                "image/jpeg",
                "/imageDir/profile.jpeg".getBytes()
        );
    }

    @Test
    @DisplayName("회원가입 성공: 모든 파라미터가 유효하면 200 OK와 성공 메시지를 반환한다.")
    void register_success() throws Exception {
        // given
        MockMultipartFile dtoFile = createDtoFile();
        MockMultipartFile imageFile = createImageFile();

        // 유효성 검사는 컨트롤러의 책임이 아니다
        doNothing().when(availabilityService).checkRegisterAvailability(any(), any());
        doNothing().when(userService).register(any(), any());


        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.POST, "/users")
                        .file(dtoFile)
                        .file(imageFile)
                        .with(csrf()) // Spring Security 가 활성화 되어있다면 필요
        );


        // then (검증)
        result.andExpect(status().isOk()) // HTTP 200 확인
                .andExpect(jsonPath("$.message").value("register_success")) // 응답 메시지 확인
                .andDo(print()); // 로그 출력

        // 핵심: 컨트롤러가 서비스를 진짜 호출했는지 검증
        verify(availabilityService).checkRegisterAvailability(any(JoinRequest.class), any());
        verify(userService).register(any(JoinRequest.class), any());
    }

    @Test
    @DisplayName("회원가입 실패 : 이메일이 중복되면 409 CONFLICT 반환한다")
    void register_fail_email_duplicated() throws Exception {
        // given
        MockMultipartFile dtoFile = createDtoFile();
        MockMultipartFile imageFile = createImageFile();

        doThrow(new ApiException(UserError.EXISTING_EMAIL))
                .when(availabilityService).checkRegisterAvailability(any(), any());

        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.POST, "/users")
                        .file(dtoFile)
                        .file(imageFile)
                        .with(csrf())
        );

        // then
        result.andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 : 닉네임이 중복되면 409 CONFLICT 반환한다")
    void register_fail_nickname_duplicated() throws Exception {
        // given
        MockMultipartFile dtoFile = createDtoFile();
        MockMultipartFile imageFile = createImageFile();

        doThrow(new ApiException(UserError.EXISTING_NICKNAME))
                .when(availabilityService).checkRegisterAvailability(any(), any());

        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.POST, "/users")
                        .file(dtoFile)
                        .file(imageFile)
                        .with(csrf())
        );

        // then
        result.andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 : 첨부한 이미지 크기가 10MB 초과하면 400 BAD_REQUEST 반환한다")
    void register_fail_image_size_bigger_than_10MB() throws Exception {
        // given
        MockMultipartFile dtoFile = createDtoFile();
        MockMultipartFile imageFile = createImageFile();

        doThrow(new ApiException(FileError.IMAGE_SIZE_TOO_BIG))
                .when(availabilityService).checkRegisterAvailability(any(), any());

        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.POST, "/users")
                        .file(dtoFile)
                        .file(imageFile)
                        .with(csrf())
        );

        // then
        result.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 실패 : 첨부한 이미지 찾을 수 없으면 404 NOT_FOUND 반환한다")
    void register_fail_image_not_found() throws Exception {
        // given
        MockMultipartFile dtoFile = createDtoFile();
        MockMultipartFile imageFile = createImageFile();

        doThrow(new ApiException(FileError.IMAGE_NOT_FOUND))
                .when(availabilityService).checkRegisterAvailability(any(), any());

        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.POST, "/users")
                        .file(dtoFile)
                        .file(imageFile)
                        .with(csrf())
        );

        // then
        result.andExpect(status().isNotFound())
                .andDo(print());
    }


    @Test
    @DisplayName("회원가입 실패 : 지원하지 않는 파일 형식 첨부하면 400 BAD_REQUEST 반환한다")
    void register_fail_invalid_image_type() throws Exception {
        // given
        MockMultipartFile dtoFile = createDtoFile();
        MockMultipartFile imageFile = createImageFile();

        doThrow(new ApiException(FileError.INVALID_FILE_TYPE)) // jpeg, png 가 아니라면 발생
                .when(availabilityService).checkRegisterAvailability(any(), any());

        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.POST, "/users")
                        .file(dtoFile)
                        .file(imageFile)
                        .with(csrf())
        );

        // then
        result.andExpect(status().isBadRequest())
                .andDo(print());
    }
}
