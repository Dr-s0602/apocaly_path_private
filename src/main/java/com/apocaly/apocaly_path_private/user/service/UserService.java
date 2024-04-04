package com.apocaly.apocaly_path_private.user.service;

import com.apocaly.apocaly_path_private.security.service.TokenService;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.model.input.InputUser;
import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository, TokenService tokenService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }


    @Transactional
    public User signUpUser(InputUser inputUser) {
        userRepository.findByEmail(inputUser.getEmail())
                .ifPresent(u -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일이 이미 사용중입니다.");
                });
        return userRepository.save(createUser(inputUser));
    }

    private User createUser(InputUser user) {
        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        return User.builder()
                .id(UUID.randomUUID())
                .email(user.getEmail())
                .password(encodedPassword)
                .isDelete(false)
                .isActivated(false)
                .isEmailVerified(false)
                .build();
    }

    @Transactional
    public ResponseEntity<?> login(InputUser inputUser) {
        User user = validateUser(inputUser);

        // 여기에 토큰 발급 로직을 구현할 수 있습니다.
        String token = tokenService.generateToken(user);

        // 로그인에 성공했을 때의 응답 객체
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("message", "로그인에 성공했습니다.");
        successResponse.put("token", token); // 토큰 추가
        return ResponseEntity.ok(successResponse);
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
        if (!bCryptPasswordEncoder.matches(inputUser.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."
            );
        }
        return user;
    }

}
