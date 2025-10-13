package ktb3.fullstack.week4.common.error;


import org.springframework.http.HttpStatus;

public enum FileUploadError implements ErrorCode {

    IMAGE_SIZE_TOO_BIG (HttpStatus.BAD_REQUEST, "image_size_too_big"),
    INVALID_FILE_TYPE (HttpStatus.BAD_REQUEST, "invalid_file_type");

    private final HttpStatus status;
    private final String key;

    FileUploadError(HttpStatus status, String key) {
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
