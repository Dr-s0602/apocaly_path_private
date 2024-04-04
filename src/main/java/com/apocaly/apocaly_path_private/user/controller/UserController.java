package com.apocaly.apocaly_path_private.user.controller;

import com.apocaly.apocaly_path_private.user.model.input.SingUpUser;
import com.apocaly.apocaly_path_private.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> singUpUser (@RequestBody SingUpUser user){
        return userService.signUpUser(user);
    }
}
