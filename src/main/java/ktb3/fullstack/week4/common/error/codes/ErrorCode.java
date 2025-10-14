package ktb3.fullstack.week4.common.error.codes;


import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();
    String getMessage();
    default int getHttpCode() {
        return getStatus().value();
    }
}
