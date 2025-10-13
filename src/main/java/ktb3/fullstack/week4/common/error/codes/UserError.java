package ktb3.fullstack.week4.common.error.codes;

import ktb3.fullstack.week4.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserError implements ErrorCode {

    CANNOT_FOUND_USER(HttpStatus.NOT_FOUND, "cannot_found_user"),
    EXISTING_EMAIL(HttpStatus.CONFLICT, "existing_email"),
    EXISTING_NICKNAME(HttpStatus.CONFLICT, "existing_nickname");

    private final HttpStatus status;
    private final String key;

    UserError(HttpStatus status, String key) {
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
