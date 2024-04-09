package com.apocaly.apocaly_path_private.user.repository;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.model.input.InputUser;
import com.apocaly.apocaly_path_private.user.model.output.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// 스프링 컨테이너에 의해 관리되는 서비스 컴포넌트로 선언합니다.
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // 사용자 정보를 조회하기 위한 UserRepository 인터페이스입니다.
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // 비밀번호를 암호화하기 위한 BCryptPasswordEncoder입니다.

    // 생성자를 통해 UserRepository와 BCryptPasswordEncoder의 인스턴스를 주입받습니다.
    public CustomUserDetailsService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    // UserDetailsService 인터페이스의 메서드를 구현합니다. 사용자 이름을 기반으로 사용자 정보를 로드합니다.
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 사용자 인증을 위한 사용자 검증 로직을 실행합니다.
        User userData = validateUser(new InputUser(username));
        // CustomUserDetails 객체를 생성하여 반환합니다. 이 객체는 Spring Security에 의해 사용자 인증 과정에서 사용됩니다.
        return new CustomUserDetails(userData);
    }

    // 사용자의 유효성을 검증하는 메서드입니다.
    private User validateUser(InputUser inputUser){
        // 주어진 이메일로 사용자를 조회합니다. 사용자가 존재하지 않을 경우 UsernameNotFoundException 예외를 발생시킵니다.
        User user = userRepository.findByEmail(inputUser.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다: " + inputUser.getEmail()));
        // 사용자 계정이 삭제된 경우 UsernameNotFoundException 예외를 발생시킵니다.
        if (user.getIsDelete()) {
            throw new DisabledException ("삭제된 계정입니다: " + inputUser.getEmail());
        }
        // 사용자 계정이 활성화되지 않은 경우 UsernameNotFoundException 예외를 발생시킵니다.
        if (user.getIsActivated()) {
            throw new LockedException("활성화되지 않은 계정입니다: " + inputUser.getEmail());
        }
        return user; // 유효한 사용자 정보를 반환합니다.
    }

}
