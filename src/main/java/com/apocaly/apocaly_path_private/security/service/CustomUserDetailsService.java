package com.apocaly.apocaly_path_private.security.service;

import com.apocaly.apocaly_path_private.user.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.apocaly.apocaly_path_private.user.model.entity.User user = userRepository.findById(UUID.fromString(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}
