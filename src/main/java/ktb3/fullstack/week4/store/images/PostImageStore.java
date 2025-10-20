package ktb3.fullstack.week4.store.images;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PostImageStore implements ImageStore {
    private final Map<String, byte[]> postImageMap = new ConcurrentHashMap<>();

    // 게시글 사진의 등록/수정 겸용 메소드
    @Override
    public void uploadImage(String postImageUrl, byte[] imageByte) {
        postImageMap.put(postImageUrl, imageByte);
    }

    @Override
    public byte[] deleteImage(String existingPostImageUrl) {
        return postImageMap.remove(existingPostImageUrl);
    }
}
