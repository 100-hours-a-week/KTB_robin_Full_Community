package ktb3.fullstack.week4.common.error;

import org.springframework.http.HttpStatus;

public enum PostError implements ErrorCode {

    CANNOT_FOUND_POST(HttpStatus.NOT_FOUND, "cannot_found_post"),
    CANNOT_EDIT_OTHERS_POST(HttpStatus.FORBIDDEN, "cannot_edit_others_post"),
    CANNOT_DELETE_OTHERS_POST(HttpStatus.FORBIDDEN, "cannot_delete_others_post");

    private final HttpStatus status;
    private final String key;

    PostError(HttpStatus status, String key) {
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
