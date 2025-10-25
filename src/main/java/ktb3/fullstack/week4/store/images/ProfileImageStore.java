package ktb3.fullstack.week4.store.images;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProfileImageStore implements ImageStore {
    private final Map<String, byte[]> profileImageMap = new ConcurrentHashMap<>();

    // 프로필 사진의 등록/수정 겸용 메소드
    @Override
    public void uploadImage(String profileImageUrl, byte[] imageByte) {
        profileImageMap.put(profileImageUrl, imageByte);
    }

    @Override
    public byte[] deleteImage(String existingProfileImageUrl) {
        return profileImageMap.remove(existingProfileImageUrl);
    }
}
