package ktb3.fullstack.week4.service.images;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostImageService implements ImageService {

    @Value("${file.postDir}")
    private String folderPath;

    @Override
    public String makeImagePathString(MultipartFile newPostImage) {
        return folderPath + UUID.randomUUID() + newPostImage.getOriginalFilename();
    }

    @Override
    public void transferImageToLocalDirectory(MultipartFile image, String postImageUrl) {
        System.out.println("경로 = " + postImageUrl);
        try {
            image.transferTo(new File(postImageUrl));
        } catch (IOException e) {
            System.out.println("이미지 이동 중 문제 발생!");
            log.info("이미지 이동 중 문제 발생!");
        }
    }
}
