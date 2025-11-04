package ktb3.fullstack.week4.service.users;

import ktb3.fullstack.week4.common.security.PasswordHasher;
import ktb3.fullstack.week4.domain.images.ProfileImage;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.dto.users.*;
import ktb3.fullstack.week4.repository.images.ProfileImageRepository;
import ktb3.fullstack.week4.repository.users.UserRepository;
import ktb3.fullstack.week4.service.errors.ErrorCheckServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDomainBuilder userDomainBuilder;

    private final PasswordHasher passwordHasher;

    private final ErrorCheckServiceImpl errorCheckService;

    private final UserRepository userRepository;

    private final ProfileImageRepository profileImageRepository;

    private final UserDeleteFacade userDeleteFacade;


    @Value("${file.profileDir}")
    private String folderPath;


    public void register(JoinRequest dto) {
        String hashedPassword = passwordHasher.hash(dto.getPassword());
        User user = userDomainBuilder.buildUser(hashedPassword, dto);
        userRepository.save(user);
    }

    // 이메일, 닉네임 조회
    public UserEditPageResponse getUserInfoForEditPage(long userId) {
        User user = errorCheckService.checkCanNotFoundUser(userId);
        UserEditPageResponse dto = userDomainBuilder.buildUserPageResponse(user.getEmail(), user.getNickname());
        return dto;
    }

    // 닉네임 변경: 존재 확인 → 저장소 갱신
    public NicknameUpdateResponse changeNickname(long userId, NicknameUpdateRequest dto) {
        User user = errorCheckService.checkCanNotFoundUser(userId);
        String newNickname = dto.getNewNickname();
        user.changeNickName(newNickname);
        return new NicknameUpdateResponse(newNickname);
    }


    @Transactional
    public void changePassword(long userId, PasswordUpdateRequest newPassword) {
        User user = errorCheckService.checkCanNotFoundUser(userId);
        String hashedPassword = passwordHasher.hash(newPassword.getNewPassword());
        user.changePassword(hashedPassword);
    }

    // 프로필 이미지 변경: 존재 확인 후 저장(향후 Store/Repository 메서드 추가 시 연결)
    @Transactional
    public String changeProfileImage(long userId, MultipartFile newProfileImage) {
        User user = errorCheckService.checkCanNotFoundUser(userId);
        String profileImageUrl = folderPath + newProfileImage.getOriginalFilename();

        // (리팩토링) 에러 등록 및 관리 필요
        try {
            newProfileImage.transferTo(new File(profileImageUrl));
        } catch (IOException e) {
            System.out.println("이미지 이동 중 문제 발생!");
            log.info("이미지 이동 중 문제 발생!");
        }

        // 프로필 이미지 객체 생성 -> 도메인빌더 메소드로 갈아끼우기?
        ProfileImage profileImage = ProfileImage.builder()
                .imageUrl(profileImageUrl)
                .isPrimary(true)
                .displayOrder(1)
                .build();

        List<ProfileImage> profileImages = profileImageRepository.findAllByUserId(userId);
        if (!profileImages.isEmpty()) {
            for (ProfileImage image : profileImages) {
                image.adjustDisplayOrder(); // displayOrder 순서 1씩 뒤로 밀기
                if (image.isPrimary()) {
                    image.makeToNonPrimary(); // 이제 대표사진이 아니다
                }
            }
        }

        profileImage.linkUser(user); // 연관관계 편의 메소드
        profileImageRepository.save(profileImage); // 더티체킹
        return profileImageUrl;
    }

    // 프로필 이미지 삭제
    @Transactional
    public String deleteProfileImage(long userId) {
        errorCheckService.checkCanNotFoundUser(userId);
        ProfileImage primaryProfileImage = profileImageRepository.findByUserIdAndIsPrimaryIsTrue(userId);
        primaryProfileImage.deleteEntity();
        // 프로필 이미지 1장 : 삭제 -> 즉시 새로운 사진 등록 필요
        // 프로필 이미지 2장 이상 : 삭제 -> 즉시 프로필 사진 불러오기 필요

        return primaryProfileImage.getImageUrl();
    }

    // 이후개발) 프로필 사진 history 에서 선택한 이미지만 삭제


    // 회원 탈퇴
    @Transactional
    public void withdrawMemberShip(long userId) {
        errorCheckService.checkCanNotFoundUser(userId);
        userDeleteFacade.deleteUser(userId); // 연관 데이터까지 모두 Soft Delete 적용하는 퍼사드
    }
}
