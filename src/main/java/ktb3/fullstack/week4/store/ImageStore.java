package ktb3.fullstack.week4.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImageStore {
    private final Map<String, byte[]> profileImageMap = new ConcurrentHashMap<>();

    public void updateImage(String profileImageUrl, byte[] imageByte) {
        profileImageMap.put(profileImageUrl, imageByte);
    }

    public void deleteImage(String existingProfileImageUrl) {
        profileImageMap.remove(existingProfileImageUrl);
    }
}
