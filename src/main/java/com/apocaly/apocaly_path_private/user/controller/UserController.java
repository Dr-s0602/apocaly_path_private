package com.apocaly.apocaly_path_private.user.controller;

import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.model.input.InputUser;
import com.apocaly.apocaly_path_private.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    @Operation(
            summary = "Sign up a new user",
            description = "Registers a new user in the system.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User signed up successfully",
                            content = @Content(
                                    schema = @Schema(implementation = User.class),
                                    examples = @ExampleObject(
                                            value = "{\n" +
                                                    "  \"id\": \"1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p\",\n" +
                                                    "  \"email\": \"test@example.com\",\n" +
                                                    "  \"isDelete\": false,\n" +
                                                    "  \"isActivated\": false,\n" +
                                                    "  \"isEmailVerified\": false,\n" +
                                                    "  \"isAdmin\": false,\n" +
                                                    "  \"loginType\": \"local\"\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data",
                            content = @Content(
                                    schema = @Schema(
                                            type = "string",
                                            example = "이메일이 이미 사용중입니다."
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> signUpUser(@org.springframework.web.bind.annotation.RequestBody InputUser user) {
        User newUser = userService.signUpUser(user);
        return ResponseEntity.ok(newUser);
    }
}

