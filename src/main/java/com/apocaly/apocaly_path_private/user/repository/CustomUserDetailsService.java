package com.apocaly.apocaly_path_private.user.repository;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.model.input.InputUser;
import com.apocaly.apocaly_path_private.user.model.output.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User userData = validateUser(new InputUser(username));
        //UserDetails에 담아서 return하면 AutneticationManager가 검증 함
        return new CustomUserDetails(userData);

    }


    private User validateUser(InputUser inputUser) {
        User user = userRepository.findByEmail(inputUser.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다."
                ));
        if (user.getIsDelete()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "삭제된 계정입니다."
            );
        }
        if (user.getIsActivated()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "활성화되지 않은 계정입니다."
            );
        }
        return user;
    }
}
