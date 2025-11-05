package ktb3.fullstack.week4.service.images;

import ktb3.fullstack.week4.domain.images.ProfileImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileImageService {

    @Value("${file.profileDir}")
    private String folderPath;


    public String makeImagePathString(MultipartFile newProfileImage) {
        return folderPath + newProfileImage.getOriginalFilename();
    }

    public void transferImageToLocalDirectory(MultipartFile image, String profileImageUrl) {
        try {
            image.transferTo(new File(profileImageUrl));
        } catch (IOException e) {
            System.out.println("이미지 이동 중 문제 발생!");
            log.info("이미지 이동 중 문제 발생!");
        }
    }
}
