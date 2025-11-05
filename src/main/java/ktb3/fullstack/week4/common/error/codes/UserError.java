package ktb3.fullstack.week4.common.error.codes;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserError implements ErrorCode {

    DEACTIVATED_ACCOUNT(HttpStatus.BAD_REQUEST, "deactivated_account"),
    CANNOT_FOUND_USER(HttpStatus.NOT_FOUND, "cannot_found_user"),
    EXISTING_EMAIL(HttpStatus.CONFLICT, "existing_email"),
    EXISTING_NICKNAME(HttpStatus.CONFLICT, "existing_nickname");

    private final HttpStatus status;
    private final String message;

}
