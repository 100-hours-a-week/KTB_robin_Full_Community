package ktb3.fullstack.week4.context;

import ktb3.fullstack.week4.Security.context.SecurityUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    long TEST_USER_ID = 1L;
    String TEST_USER_EMAIL = "dummy123@snaver.com";
    String TEST_USER_PASSWORD = "dummyPassword123";
    String TEST_USER_NICKNAME = "dummyNickname123";
    String TEST_USER_ROLE = "USER";

    long id() default TEST_USER_ID;
    String email() default TEST_USER_EMAIL;
    String password() default TEST_USER_PASSWORD;
    String nickname() default TEST_USER_NICKNAME;
    String role() default TEST_USER_ROLE;
}


class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 실제 애플리케이션의 SecurityUser 객체 생성
        SecurityUser securityUser = new SecurityUser(
                annotation.id(),
                annotation.email(),
                annotation.password(),
                annotation.nickname(),
                "ROLE_" + annotation.role()
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(securityUser, "", securityUser.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
