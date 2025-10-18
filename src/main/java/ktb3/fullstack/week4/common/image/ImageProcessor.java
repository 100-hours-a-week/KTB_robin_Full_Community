package ktb3.fullstack.week4.common.image;

import ktb3.fullstack.week4.common.error.codes.FileUploadError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ImageProcessor {
    // 10MB 제한
    private static final long MAX_SIZE = 10L * 1024 * 1024;

    public byte[] toByteStream(MultipartFile newProfileImage) {
        try {
            // 선 검증: 스트리밍/메모리 사용 전에 요청 메타 정보로 확인
            if (newProfileImage.getSize() > MAX_SIZE) {
                throw new ApiException(FileUploadError.IMAGE_SIZE_TOO_BIG);
            }
            byte[] imageBytes = newProfileImage.getBytes();
            // 이중 검증: 실제 읽은 바이트 기준
            if (imageBytes.length > MAX_SIZE) {
                throw new ApiException(FileUploadError.IMAGE_SIZE_TOO_BIG);
            }
            return imageBytes;
        } catch (IOException e)  {
            // 파일 읽기 실패 시 타입/손상 등으로 간주
            throw new ApiException(FileUploadError.INVALID_FILE_TYPE);
        }
    }

    // 겹치지 않는 String imageUrl 만든다.
    // 예) profile/2025/10/17/7b9c1f9a2a8f4e5f8c7d6e5c4b3a2f10
    public String makeRandomImageUrl() {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String random = UUID.randomUUID().toString().replace("-", "");
        return "profile/" + datePath + "/" + random;
    }
}
