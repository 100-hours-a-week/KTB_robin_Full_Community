package ktb3.fullstack.week4.service.images;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    String makeImagePathString(MultipartFile image);
    void transferImageToLocalDirectory(MultipartFile image, String imageUrl);
}
