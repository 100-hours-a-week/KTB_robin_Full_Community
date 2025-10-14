package ktb3.fullstack.week4.common.error.codes;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileUploadError implements ErrorCode {

    IMAGE_SIZE_TOO_BIG (HttpStatus.BAD_REQUEST, "image_size_too_big"),
    INVALID_FILE_TYPE (HttpStatus.BAD_REQUEST, "invalid_file_type");

    private final HttpStatus status;
    private final String message;

}
