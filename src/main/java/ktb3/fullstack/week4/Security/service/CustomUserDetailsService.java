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

        return new SecurityUser(user);
    }
}
