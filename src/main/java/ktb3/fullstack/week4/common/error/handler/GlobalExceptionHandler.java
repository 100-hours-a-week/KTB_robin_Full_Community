package ktb3.fullstack.week4.common.error.handler;

import ktb3.fullstack.week4.common.error.codes.ErrorCode;
import ktb3.fullstack.week4.common.error.codes.GenericError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // 정의된 오류 처리
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(ApiException e) {
        ErrorCode code = e.getCode();
        return ResponseEntity.
                status(code.getStatus()).
                body(ApiResponse.error(code.getMessage()));
    }

    // 예기치 못한 오류 500 으로 일반화
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        return ResponseEntity.
                status(GenericError.INTERNAL_SERVER_ERROR.getStatus()).
                body(ApiResponse.error(GenericError.INTERNAL_SERVER_ERROR.getMessage()));
    }

}
