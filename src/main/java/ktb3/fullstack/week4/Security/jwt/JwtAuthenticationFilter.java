package ktb3.fullstack.week4.Security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ktb3.fullstack.week4.Security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public static final String EMPTY_TOKEN = "Optional.empty";


    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        try {
            // EMPTY or Bearer를 제거한 토큰 문자열
            String accessToken = String.valueOf(tokenProvider.resolveAccessTokenFromAuthorization(request));

            if (!accessToken.equals(EMPTY_TOKEN) || tokenProvider.validateAccessToken(accessToken)) {
                Authentication authentication = tokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            try {
                customAuthenticationEntryPoint.commence(request, response, e);
            } catch (IOException ex) {
                handlerExceptionResolver.resolveException(request, response, null, ex);
            }
        } catch (JwtException e) {
            try {
                customAuthenticationEntryPoint.commence(request, response, new BadCredentialsException(e.getMessage(), e));
            } catch (IOException ex) {
                handlerExceptionResolver.resolveException(request, response, null, ex);
            }
        } catch (Exception e) {
            // GlobalHandler 에서 처리
            handlerExceptionResolver.resolveException(request, response, null, e); //handlerExceptionResolver == spring mvc 예외처리기
        }
    }
}
