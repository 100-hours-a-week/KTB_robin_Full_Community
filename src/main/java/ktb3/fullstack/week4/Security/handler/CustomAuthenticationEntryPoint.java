package ktb3.fullstack.week4.Security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // 필터에서의 인증 실패 (401) 예외처리를 담당하는 클래스

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        String errorMessage = authException != null && authException.getMessage() != null
                ? authException.getMessage()
                : "인증이 필요하거나 유효하지 않은 토큰입니다.";

        ApiResponse<Void> body = ApiResponse.error(errorMessage);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 응답

        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
    }
}
