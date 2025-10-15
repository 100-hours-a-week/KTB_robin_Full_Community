package ktb3.fullstack.week4.config;

import ktb3.fullstack.week4.auth.JwtAuthInterceptor;
import ktb3.fullstack.week4.auth.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider();
    }

    @Bean
    public JwtAuthInterceptor jwtAuthInterceptor(JwtTokenProvider provider) {
        return new JwtAuthInterceptor(provider, REFRESH_COOKIE_NAME);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor(jwtTokenProvider()))
                .addPathPatterns(
                        "/users/**",
                        "/posts/**"
                )
                .excludePathPatterns(
                        "/auth/**",
                        "/availability/**",
                        "/users",            // 회원가입
                        "/posts/*",          // 상세조회는 현재 명세상 인증 필요 → 필요 시 제외 해제
                        "/error"
                );
    }

}
