package ktb3.fullstack.week4.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProfileImageStore implements ImageStore{
    private final Map<String, byte[]> profileImageMap = new ConcurrentHashMap<>();

    // 프로필 사진의 등록/수정 겸용 메소드
    @Override
    public void uploadProfileImage(String profileImageUrl, byte[] imageByte) {
        profileImageMap.put(profileImageUrl, imageByte);
    }

    @Override
    public void deleteProfileImage(String existingProfileImageUrl) {
        profileImageMap.remove(existingProfileImageUrl);
    }
}
