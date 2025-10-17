package ktb3.fullstack.week4.common.image;

import ktb3.fullstack.week4.common.error.codes.FileUploadError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.dto.common.ApiResponse;
import ktb3.fullstack.week4.dto.users.ProfileImageUrlResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ImageProcessor {
    private final int MAX_SIZE = 100_000;

    public byte[] toByteStream(MultipartFile newProfileImage) {
        byte[] imageBytes;
        try {
            imageBytes = newProfileImage.getBytes();
            if(imageBytes.length < MAX_SIZE) {
                throw new ApiException(FileUploadError.IMAGE_SIZE_TOO_BIG);
            }
        } catch (IOException e)  {
            throw new ApiException(FileUploadError.INVALID_FILE_TYPE);
        }
        return imageBytes;
    }

    // 겹치지 않는 String imageUrl 만든다.
    // 예) profile/2025/10/17/7b9c1f9a2a8f4e5f8c7d6e5c4b3a2f10
    public String makeRandomImageUrl() {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String random = UUID.randomUUID().toString().replace("-", "");
        return "profile/" + datePath + "/" + random;
    }
}
