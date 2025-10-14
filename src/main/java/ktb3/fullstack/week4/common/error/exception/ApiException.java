package ktb3.fullstack.week4.common.error.exception;

import ktb3.fullstack.week4.common.error.codes.ErrorCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ErrorCode code;

    public ApiException(ErrorCode code) {
        super(code.getMessage());
        this.code = code;
    }
}
