package ktb3.fullstack.week4.common.error.codes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentError implements ErrorCode {

    CANNOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "cannot_found_comment"),
    CANNOT_EDIT_OTHERS_COMMENT (HttpStatus.FORBIDDEN, "cannot_edit_others_comment"),
    CANNOT_DELETE_OTHERS_COMMENT (HttpStatus.FORBIDDEN, "cannot_delete_others_comment");

    private final HttpStatus status;
    private final String message;
}
