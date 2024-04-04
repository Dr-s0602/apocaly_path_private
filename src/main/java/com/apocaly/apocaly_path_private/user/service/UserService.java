package com.apocaly.apocaly_path_private.user.service;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.model.input.SingUpUser;
import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    @Autowired
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
    }

    public void checkEmailDuplication(String email) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new RuntimeException("이메일이 이미 사용중입니다.");
        }
    }


    @Transactional
    public ResponseEntity<Map<String, Object>> signUpUser(SingUpUser user) {
        Map<String, Object> responseMap = new HashMap<>();
        String email = user.getEmail();

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            responseMap.put("success", false);
            responseMap.put("message", "이메일이 이미 사용중입니다.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        String password = bCryptPasswordEncoder.encode(user.getPassword());

        User newUser = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(password)
                .isDelete(false)
                .isActivated(false)
                .isEmailVerified(false)
                .build();

        try {
            userRepository.save(newUser);
            responseMap.put("success", true);
        } catch (Exception e) {
            responseMap.put("success", false);
            responseMap.put("message", e.getMessage());
        }
        return ResponseEntity.ok(responseMap);
    }
}
