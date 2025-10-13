package ktb3.fullstack.week4.common.error;


import org.springframework.http.HttpStatus;

public enum AuthError implements ErrorCode {

    INVALID_EMAIL_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "invalid_email_or_password"),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "access_token_expired"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "refresh_token_expired");

    private final HttpStatus status;
    private final String key;

    AuthError(HttpStatus status, String key) {
        this.status = status;
        this.key = key;
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public String getKey() {
        return this.key;
    }
}
