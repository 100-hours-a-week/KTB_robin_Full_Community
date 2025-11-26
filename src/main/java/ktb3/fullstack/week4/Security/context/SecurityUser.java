package ktb3.fullstack.week4.Security.context;

import ktb3.fullstack.week4.domain.users.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    // 인증된 사용자의 식별자 (DB PK)
    private final Long id;

    // 로그인 아이디 (Email)
    private final String email;

    // 암호화된 비밀번호
    private final String password;

    // 닉네임
    private final String nickname;

    // 권한
    private final String role;

    // User 엔티티로부터 SecurityUser를 생성하는 생성자
    public SecurityUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getHashedPassword();
        this.nickname = user.getNickname();
        this.role = user.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(this.getRole()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        // 시큐리티 컨텍스트에서 principal.getUsername() 호출 시 반환될 값
        // 일반적으로 로그인 ID인 이메일을 반환
        return this.email;
    }

    // 계정 만료 여부 (true: 만료 안됨)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부 (true: 잠기지 않음)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부 (true: 만료 안됨)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 (true: 활성화)
    @Override
    public boolean isEnabled() {
        // User 엔티티에 @SQLRestriction("deleted = false")가 있으므로
        // 조회된 유저는 기본적으로 활성화된(삭제되지 않은) 유저라고 가정
        return true;
    }
}