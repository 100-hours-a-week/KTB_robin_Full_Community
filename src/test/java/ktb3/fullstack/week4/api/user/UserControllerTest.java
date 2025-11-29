package ktb3.fullstack.week4.api.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ktb3.fullstack.week4.Security.config.SecurityConfig;
import ktb3.fullstack.week4.Security.context.SecurityUser;
import ktb3.fullstack.week4.Security.jwt.JwtAuthenticationFilter;
import ktb3.fullstack.week4.Security.service.AppPasswordEncoder;
import ktb3.fullstack.week4.common.error.codes.FileError;
import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.dto.users.*;
import ktb3.fullstack.week4.service.availabilities.AvailabilityService;
import ktb3.fullstack.week4.service.users.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USER_EMAIL = "dummy123@snaver.com";
    private static final String TEST_USER_PASSWORD = "dummyPassword123";
    private static final String TEST_USER_NICKNAME = "dummyNickname123";
    private static final String TEST_USER_ROLE = "ROLE_USER";

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig { // @AuthenticationPrincipal 작동을 위한 최소 설정 정의

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    /**
     * 인증된 사용자를 MockMvc 요청에 주입하기 위한 헬퍼 메서드
     */
    private RequestPostProcessor mockUser() {
        SecurityUser securityUser = new SecurityUser(
                TEST_USER_ID, TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NICKNAME, TEST_USER_ROLE
        );
        return authentication(new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
    }

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
    AppPasswordEncoder passwordEncoder;

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
                "/assets/images/profile/profile.jpeg".getBytes()
        );
    }

    private MockMultipartFile createImageFile(String name) {
        return new MockMultipartFile(
                name,
                "profile.jpeg",
                "image/jpeg",
                "/assets/images/profile/profile.jpeg".getBytes()
        );
    }

    @Test
    @DisplayName("회원가입 성공: 모든 파라미터가 유효하면 200 OK와 성공 메시지를 반환한다.")
    void register_success() throws Exception {
        // given
        MockMultipartFile dtoFile = createDtoFile();
        MockMultipartFile imageFile = createImageFile();

        doNothing().when(availabilityService).checkRegisterAvailability(any(), any());
        doNothing().when(userService).register(any(), any());


        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.POST, "/users")
                        .file(dtoFile)
                        .file(imageFile)
                        .with(csrf())
        );


        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("register_success"))
                .andDo(print());

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


    @Test
    @DisplayName("회원 정보 (email, nickname) 가져오기 성공 : 인증된 사용자가 정보 가져오기 API를 호출하면 200 OK 와 데이터(email, nickname)를 반환한다")
    void get_user_info_success() throws Exception {
        // given
        UserEditPageResponse response = new UserEditPageResponse(TEST_USER_EMAIL, TEST_USER_NICKNAME);
        given(userService.getUserInfoForEditPage(TEST_USER_ID)).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(
                get("/users/me")
                        .with(mockUser())
                        .with(csrf())
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("userinfo_fetch_success"))
                .andExpect(jsonPath("$.data.email").value(TEST_USER_EMAIL))
                .andExpect(jsonPath("$.data.nickname").value(TEST_USER_NICKNAME))
                .andDo(print());

        verify(userService).getUserInfoForEditPage(TEST_USER_ID);
    }


    @Test
    @DisplayName("회원탈퇴 성공 : 인증된 사용자가 탈퇴 API를 호출하면 UserSerivce 의 withdrawMemberShip 메소드를 호출하고, 200 OK 를 반환한다")
    void withdraw_membership_success() throws Exception {
        // given
        doNothing().when(userService).withdrawMemberShip(TEST_USER_ID);

        // when
        ResultActions result = mockMvc.perform(
                delete("/users/me")
                        .with(mockUser())
                        .with(csrf())
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("membership_withdraw_success"))
                .andDo(print());

        verify(userService).withdrawMemberShip(TEST_USER_ID);
    }


    @Test
    @DisplayName("회원 닉네임 수정 성공 : 닉네임 수정에 성공하면 200 OK 와 함께, 새로운 닉네임과 성공 메시지를 응답받는다")
    void change_nickname_success() throws Exception {
        // given
        String newNickname = "newNickname123";
        NicknameUpdateRequest dto = new NicknameUpdateRequest(newNickname);
        NicknameUpdateResponse expected = new NicknameUpdateResponse(newNickname);

        // [수정] 객체 참조값(Reference)이 다르므로 dto 대신 any() 매처를 사용해야 Mock이 정상 동작함
        doNothing().when(availabilityService).checkNewNicknameAvailability(any(NicknameUpdateRequest.class));
        given(userService.changeNickname(eq(TEST_USER_ID), any(NicknameUpdateRequest.class)))
                .willReturn(expected);

        // when
        ResultActions result = mockMvc.perform(
                patch("/users/me/nickname")
                        .with(mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf())
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("nickname_edit_success"))
                .andExpect(jsonPath("$.data.newNickname").value(newNickname)) // 이제 data가 null이 아님
                .andDo(print());

        // 검증 시에도 any() 사용
        verify(availabilityService).checkNewNicknameAvailability(any(NicknameUpdateRequest.class));
        verify(userService).changeNickname(eq(TEST_USER_ID), any(NicknameUpdateRequest.class));
    }

    @Test
    @DisplayName("회원 닉네임 수정 실패 : 이미 존재하는 닉네임을 입력하면 수정에 실패하고 409 CONFLICT 를 반환한다")
    void change_nickname_fail_for_existing_nickname() throws Exception {
        // given
        String newNickname = TEST_USER_NICKNAME;
        NicknameUpdateRequest dto = new NicknameUpdateRequest(newNickname);

        doThrow(new ApiException(UserError.EXISTING_NICKNAME))
                .when(availabilityService).checkNewNicknameAvailability(any(NicknameUpdateRequest.class));

        // when
        ResultActions result = mockMvc.perform(
                patch("/users/me/nickname")
                        .with(mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf())
        );

        // then
        result.andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("회원 비밀번호 수정 성공 : 비밀번호 수정에 성공하면 200 OK 와 함께, 성공 메시지를 응답받는다")
    void change_password_success() throws Exception {
        // given
        String newPassword = "newPassword123";
        PasswordUpdateRequest dto = new PasswordUpdateRequest(newPassword);

        doNothing().when(userService).changePassword(eq(TEST_USER_ID), any(PasswordUpdateRequest.class));

        // when
        ResultActions result = mockMvc.perform(
                patch("/users/me/password")
                        .with(mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf())
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("password_edit_success"))
                .andDo(print());

        // 검증 시에도 any() 사용
        verify(userService).changePassword(eq(TEST_USER_ID), any(PasswordUpdateRequest.class));
    }

    @Test
    @DisplayName("회원 비밀번호 수정 실패 : 비어있는 비밀번호로 요청을 보낼경우, @Valid 조건을 통과하지 못하여 400 BAD_REQUEST 와 에러 메시지를 반환한다")
    void change_password_fail() throws Exception {
        // given
        String newPassword = "";
        PasswordUpdateRequest dto = new PasswordUpdateRequest(newPassword);

        // when
        ResultActions result = mockMvc.perform(
                patch("/users/me/password")
                        .with(mockUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf())
        );

        // then : GlobalExceptionHandler 에서 MethodArgumentNotValidException 예외를 처리하여 400 응답을 반환한다
        result.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 이미지 변경 성공 : 유효한 이미지 파일을 업로드하면 200 OK 와 변경된 이미지 URL을 반환한다")
    void register_new_profile_image_success() throws Exception {
        // given
        MockMultipartFile newProfileImage = new MockMultipartFile(
                "profile_image",
                "profile.jpeg",
                "image/jpeg",
                "/assets/images/profile/profile.jpeg".getBytes()
        );
        String savedUrl = "/assets/images/profile/new_profile.jpeg";

        given(userService.changeProfileImage(eq(TEST_USER_ID), any(MultipartFile.class)))
                .willReturn(savedUrl);

        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.PATCH, "/users/me/profile-image")
                        .file(newProfileImage)
                        .with(mockUser())
                        .with(csrf())
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("profile_image_upload_success"))
                .andExpect(jsonPath("$.data.profileImageUrl").value(savedUrl))
                .andDo(print());

        verify(userService).changeProfileImage(eq(TEST_USER_ID), any(MultipartFile.class));
    }

    @Test
    @DisplayName("프로필 이미지 변경 실패 : 지원하지 않는 파일 형식을 업로드하면 400 BAD_REQUEST 를 반환한다")
    void register_new_profile_image_fail_invalid_type() throws Exception {
        // given
        String multipartFileName = "profile_image";
        MockMultipartFile newProfileImage = createImageFile(multipartFileName);

        doThrow(new ApiException(FileError.INVALID_FILE_TYPE))
                .when(userService).changeProfileImage(eq(TEST_USER_ID), any(MultipartFile.class));

        // when
        ResultActions result = mockMvc.perform(
                multipart(HttpMethod.PATCH, "/users/me/profile-image")
                        .file(newProfileImage)
                        .with(mockUser())
                        .with(csrf())
        );

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("invalid_file_type"))
                .andDo(print());
    }

    @Test
    @DisplayName("프로필 이미지 삭제 성공 : 프로필 이미지를 삭제하면 200 OK 와 삭제된 이미지 URL을 반환한다")
    void remove_profile_image_success() throws Exception {
        // given
        String deletedUrl = "/assets/images/profile/deleted.jpeg";
        given(userService.deleteProfileImage(TEST_USER_ID)).willReturn(deletedUrl);

        // when
        ResultActions result = mockMvc.perform(
                delete("/users/me/profile-image")
                        .with(mockUser())
                        .with(csrf())
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("profile_image_delete_success"))
                .andExpect(jsonPath("$.data.profileImageUrl").value(deletedUrl))
                .andDo(print());

        verify(userService).deleteProfileImage(TEST_USER_ID);
    }
}
