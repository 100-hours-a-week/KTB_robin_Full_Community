package ktb3.fullstack.week4.store.images;

public interface ImageStore {
    void uploadImage(String imageUrl, byte[] imageByte);
    byte[] deleteImage(String existingImageUrl);
}
