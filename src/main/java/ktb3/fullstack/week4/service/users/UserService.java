package ktb3.fullstack.week4.service;

import ktb3.fullstack.week4.common.error.codes.UserError;
import ktb3.fullstack.week4.common.error.exception.ApiException;
import ktb3.fullstack.week4.common.security.PasswordHasher;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.users.*;
import ktb3.fullstack.week4.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Value("${file.profileDir}")
    private String folderPath;

    public void register(JoinRequest dto) {
        String hashedPassword = passwordHasher.hash(dto.getPassword());
        /*
        * OCP 관점)
        *   문제점 : 생성자의 매개변수 순서가 바뀐다면 Service 코드도 수정 되어야함.
        *   개선 : 빌더 패턴을 사용하여 멤버 이름으로 명시적 초기화
        *       -> 이제 UserService 는 User 생성자의 매개변수 순서를 몰라도 된다
        * */
        User user = User.builder()
                .email(dto.getEmail())
                .hashedPassword(hashedPassword)
                .nickname(dto.getNickname())
                .build();

        userRepository.save(user);
    }

    // 이메일, 닉네임 조회
    public UserEditPageResponse getUserInfoForEditPage(long userId) {
        User user = checkCanNotFoundUser(userId);

        UserEditPageResponse dto = UserEditPageResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();

        return dto;
    }

    // 닉네임 변경: 존재 확인 → 저장소 갱신
    public NicknameUpdateResponse changeNickname(long userId, NicknameUpdateRequest dto) {
        User user = checkCanNotFoundUser(userId);

        String newNickname = dto.getNewNickname();
        String oldNickname = user.getNickname();
        user.changeNickName(newNickname);
        return new NicknameUpdateResponse(newNickname);
    }

    // 비밀번호 변경: 존재 확인 → 해시 적용 → 저장(향후 Store/Repository 메서드 추가 시 연결)
    // 여기서 비밀번호를 변경한다면, @Tranasctional의 범위 안에있고,
    // 영속성 컨텍스트가 관리하는 user를 꺼낼경우 save 하지 않아도됨
    // 이거 테스트 코드 필요하다.
    @Transactional
    public void changePassword(long userId, PasswordUpdateRequest newPassword) {
        User user = checkCanNotFoundUser(userId); // 이 객체는 영속성 컨텍스트의 관리를 받을것인가?
        String hashedPassword = passwordHasher.hash(newPassword.getNewPassword());
        user.changePassword(hashedPassword);
//        userRepository.updatePassword(user); // 내 예상 : user 에 대한 더티체킹이 일어나지 않아, 디비에는 반영되지 않는다.
    }

    // 프로필 이미지 변경: 존재 확인 후 저장(향후 Store/Repository 메서드 추가 시 연결)
    @Transactional
    public String changeProfileImage(long userId, MultipartFile newProfileImage) {

        User user = checkCanNotFoundUser(userId);

        String profileImageUrl = folderPath + newProfileImage.getOriginalFilename();

        // (리팩토링) 에러 등록 및 관리 필요
        try {
            newProfileImage.transferTo(new File(profileImageUrl));
        } catch (IOException e) {
            System.out.println("이미지 이동 중 문제 발생!");
            log.info("이미지 이동 중 문제 발생!");
        }

        // 프로필 이미지 객체 생성
        ProfileImage profileImage = ProfileImage.builder()
                .user(user)
                .imageUrl(profileImageUrl)
                .build();

        // 리포지토리 생성 후 DB 에 반영
//        profileImageRepository.save(profileImage);

        user.addProfileImage(profileImage); // 더티체킹 동작

        return profileImageUrl;
    }

    // 프로필 이미지 삭제
    @Transactional
        public String deleteProfileImage(long userId) {
        User user = checkCanNotFoundUser(userId);
        // 로컬 폴더에서 이미지 삭제
        // -> 이 부분도 나중에 대표이미지 속성을 추가해서 그걸로 판별하고,
        // order 그 다음속성이 대표가되고, 이런식의 로직 처리를 해야할듯.
        // 그러기 위해서는 일단 primaryImage, display_order 컬럼/속성이 필요하다.
//        ProfileImage targetImage =
        Path urlToDelete = Paths.get(user.getProfileImages().getFirst().getImageUrl());
        // user 의 이미지 url 제거


        String existingProfileImageUrl = user.getProfileImageUrl();
        user.changeProfileImage(null);
        profileImageStore.deleteImage(existingProfileImageUrl);
        return existingProfileImageUrl;
    }

    // 이후개발) 프로필 사진 history 에서 선택한 이미지만 삭제


    // 회원 탈퇴 : 사용자 삭제
    @Transactional
    public void withdrawMemberShip(long userId) {
        checkCanNotFoundUser(userId);
        userRepository.deleteById(userId);
    }

    // 상속을 기반으로 동작하는 CGLIB (런타임 프록시) 를 위해서는 private 접근제어자 사용불가
    @Transactional(readOnly = true)
    protected User checkCanNotFoundUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(UserError.CANNOT_FOUND_USER));
        return user;
    }
}
