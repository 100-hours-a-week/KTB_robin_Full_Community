package ktb3.fullstack.week4.config.swagger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 어노테이션이 붙은 클래스/메소드의 Swagger 문서에
 * 400 invalid_request, 500 internal_server_error 예시를 공통으로 추가합니다.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommonErrorResponses {
}
