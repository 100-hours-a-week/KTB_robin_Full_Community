package ktb3.fullstack.week4.Security.service;

import ktb3.fullstack.week4.Security.context.SecurityUser;
import ktb3.fullstack.week4.domain.users.User;
import ktb3.fullstack.week4.repository.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long userId = Long.parseLong(username);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        // 시큐리티 유저의 getAuthorities() 를 호출하면, 권한은 자동으로 들어갈 것인가
        // 혹은 생성자에 user의 권한을 명시적으로 넣지 않았으므로 안들어갈 것인가.
        // 당연히 후자겠죠?
        return new SecurityUser(user);
    }
}
