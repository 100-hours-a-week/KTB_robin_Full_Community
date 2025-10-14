package ktb3.fullstack.week4.common.error.codes;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthError implements ErrorCode {

    INVALID_EMAIL_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "invalid_email_or_password"),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "access_token_expired"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "refresh_token_expired");

    private final HttpStatus status;
    private final String message;
}
