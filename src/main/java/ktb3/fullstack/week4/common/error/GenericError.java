package ktb3.fullstack.week4.common.error;


import org.springframework.http.HttpStatus;

public enum GenericError implements ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "invalid_request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "not_found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");

    private final HttpStatus status;
    private final String key;

    GenericError(HttpStatus status, String key) {
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
