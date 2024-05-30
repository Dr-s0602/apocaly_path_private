package com.apocaly.apocaly_path_private.user.service;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.model.input.InputUser;
import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;

    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
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
                .isAdmin(false)
                .build();
    }

    @Transactional
    public Optional<User> findByEmail(String username) {
        return userRepository.findByEmail(username);
    }

    @Transactional
    public User signUpSnsUser(User user) {
        userRepository.findByEmail(user.getEmail())
                .ifPresent(u -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이메일이 이미 사용중입니다.");
                });
        return userRepository.save(user);
    }
}
