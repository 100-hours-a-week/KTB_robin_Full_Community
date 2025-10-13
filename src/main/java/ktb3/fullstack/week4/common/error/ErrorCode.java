package ktb3.fullstack.week4.common.error;


import org.springframework.http.HttpStatus;

public interface ErrorCode {
    HttpStatus getStatus();
    String getKey();
    default int getHttpCode() {
        return getStatus().value();
    }
}
