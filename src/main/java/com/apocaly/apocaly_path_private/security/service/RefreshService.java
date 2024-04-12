package com.apocaly.apocaly_path_private.security.service;

import com.apocaly.apocaly_path_private.security.model.entity.RefreshToken;
import com.apocaly.apocaly_path_private.security.repository.RefreshRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
public class RefreshService {
    private final RefreshRepository refreshRepository;

    public RefreshService(RefreshRepository refreshRepository) {
        this.refreshRepository = refreshRepository;
    }

    public RefreshToken save(RefreshToken refreshToken) {
        return refreshRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByTokenValue(String token) {
        return refreshRepository.findByTokenValue(token);
    }
}
