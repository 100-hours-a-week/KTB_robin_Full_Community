package ktb3.fullstack.week4.common.error.codes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostError implements ErrorCode {

    POST_ID_INVALID(HttpStatus.BAD_REQUEST, "post_id_is_invalid"),
    CANNOT_FOUND_POST(HttpStatus.NOT_FOUND, "cannot_found_post"),
    CANNOT_EDIT_OTHERS_POST(HttpStatus.FORBIDDEN, "cannot_edit_others_post"),
    CANNOT_DELETE_OTHERS_POST(HttpStatus.FORBIDDEN, "cannot_delete_others_post");

    private final HttpStatus status;
    private final String message;
}
