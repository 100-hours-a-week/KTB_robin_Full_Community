package ktb3.fullstack.week4.common.error.codes;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GenericError implements ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "invalid_request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "forbidden"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "not_found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error");

    private final HttpStatus status;
    private final String message;
}
