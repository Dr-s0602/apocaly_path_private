package com.apocaly.apocaly_path_private.user.controller;

import com.apocaly.apocaly_path_private.security.jwt.util.JWTUtil;
import com.apocaly.apocaly_path_private.security.model.entity.RefreshToken;
import com.apocaly.apocaly_path_private.security.service.RefreshService;
import com.apocaly.apocaly_path_private.user.model.entity.User;
import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import com.apocaly.apocaly_path_private.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.redirect-signup-uri}")
    private String kakaoRedirectSignupUri;

    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshService refreshService;
    private final JWTUtil jwtUtil;

    public AuthController(UserRepository userRepository, UserService userService, RefreshService refreshService, AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.refreshService = refreshService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/kakao/callback")
    public void kakaoLogin(@RequestParam String code, HttpServletResponse response) throws IOException, JSONException {
        log.info("code = {}", code);

        // 액세스 토큰을 요청하기 위한 URL 및 헤더 설정
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String tokenRequestBody = "grant_type=authorization_code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&code=" + code;

        // 토큰 요청
        HttpEntity<String> tokenRequestEntity = new HttpEntity<>(tokenRequestBody, tokenHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, tokenRequestEntity, String.class);
        log.info("token response = {}", tokenResponse.getBody());

        JSONObject tokenJson = new JSONObject(tokenResponse.getBody());
        String accessToken = tokenJson.getString("access_token");
        log.info("accessToken = {}", accessToken);

        // 사용자 정보를 요청하기 위한 URL 및 헤더 설정
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.set("Authorization", "Bearer " + accessToken);

        // 사용자 정보 요청
        HttpEntity<String> userInfoRequestEntity = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequestEntity, String.class);
        log.info("user info response = {}", userInfoResponse.getBody());

        JSONObject userJson = new JSONObject(userInfoResponse.getBody());
        String email = userJson.getJSONObject("kakao_account").has("email") ?
                userJson.getJSONObject("kakao_account").getString("email") : "이메일 정보가 없습니다.";
        log.info("email = {}", email);

        Optional<User> optionalUser = userRepository.findByEmailAndLoginType(email, "kakao");

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setLastLogin(LocalDateTime.now());
            user.setSnsAccessToken(accessToken);
            userRepository.save(user);

            // JWT 토큰 발급
            Long accessExpiredMs = 600000L;
            String accessTokenJwt = jwtUtil.generateToken(email, "access", accessExpiredMs);
            Long refreshExpiredMs = 86400000L;
            String refreshTokenJwt = jwtUtil.generateToken(email, "refresh", refreshExpiredMs);

            RefreshToken refreshToken = RefreshToken.builder()
                    .id(UUID.randomUUID())
                    .status("activated")
                    .userAgent(response.getHeader("User-Agent"))
                    .user(user)
                    .tokenValue(refreshTokenJwt)
                    .expiresIn(refreshExpiredMs)
                    .build();

            refreshService.save(refreshToken);

            // 로그인 성공 후 URL에 토큰 정보 포함
            String redirectUrl = String.format("http://localhost:3000/user/success?access=%s&refresh=%s&isAdmin=%s",
                    accessTokenJwt, refreshTokenJwt, user.getIsAdmin());

            response.sendRedirect(redirectUrl);
            log.info("로그인 성공: {}", email);
        } else {
            log.info("회원가입 필요: {}", email);
            response.sendRedirect("http://localhost:3000/user");
        }
    }

    @GetMapping("/kakao/signup/callback")
    public void kakaoSignup(@RequestParam String code, HttpServletResponse response) throws IOException, JSONException {
        log.info("code = {}", code);

        // 액세스 토큰을 요청하기 위한 URL 및 헤더 설정
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String tokenRequestBody = "grant_type=authorization_code"
                + "&client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectSignupUri
                + "&code=" + code;

        // 토큰 요청
        HttpEntity<String> tokenRequestEntity = new HttpEntity<>(tokenRequestBody, tokenHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, tokenRequestEntity, String.class);
        log.info("token response = {}", tokenResponse.getBody());

        JSONObject tokenJson = new JSONObject(tokenResponse.getBody());
        String accessToken = tokenJson.getString("access_token");
        log.info("accessToken = {}", accessToken);

        // 사용자 정보를 요청하기 위한 URL 및 헤더 설정
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.set("Authorization", "Bearer " + accessToken);

        // 사용자 정보 요청
        HttpEntity<String> userInfoRequestEntity = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequestEntity, String.class);
        log.info("user info response = {}", userInfoResponse.getBody());

        JSONObject userJson = new JSONObject(userInfoResponse.getBody());
        String email = userJson.getJSONObject("kakao_account").has("email") ?
                userJson.getJSONObject("kakao_account").getString("email") : "이메일 정보가 없습니다.";
        log.info("email = {}", email);

        Optional<User> optionalUser = userRepository.findByEmailAndLoginType(email, "kakao");

        if (optionalUser.isPresent()) {
            log.info("이미 등록된 사용자: {}", email);
            response.sendRedirect("http://localhost:3000/user");
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setLoginType("kakao");
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setIsDelete(false);
            newUser.setIsActivated(false);
            newUser.setIsEmailVerified(false);
            newUser.setIsAdmin(false);
            newUser.setPassword("");
            userRepository.save(newUser);
            log.info("회원가입 성공: {}", email);

            // 카카오 로그아웃 처리
            String logoutUrl = "https://kapi.kakao.com/v1/user/logout";
            HttpHeaders logoutHeaders = new HttpHeaders();
            logoutHeaders.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> logoutRequestEntity = new HttpEntity<>(logoutHeaders);
            ResponseEntity<String> logoutResponse = restTemplate.exchange(logoutUrl, HttpMethod.POST, logoutRequestEntity, String.class);
            log.info("logout response = {}", logoutResponse.getBody());

            response.sendRedirect("http://localhost:3000/user/login");
        }
    }
}
