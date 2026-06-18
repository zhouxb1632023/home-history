package com.homehistory.service;

import com.homehistory.dto.LoginRequest;
import com.homehistory.dto.LoginResponse;
import com.homehistory.dto.ProfileUpdateRequest;
import com.homehistory.dto.RegisterRequest;
import com.homehistory.model.User;
import com.homehistory.repository.UserRepository;
import com.homehistory.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final FileService fileService;

    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getNickname() != null ? req.getNickname() : req.getUsername())
                .build();
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername(), user.getNickname(), user.getAvatarUrl(), user.getRole().name());
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        String token = jwtUtil.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername(), user.getNickname(), user.getAvatarUrl(), user.getRole().name());
    }

    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(String username, ProfileUpdateRequest req) {
        User user = getCurrentUser(username);
        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        return userRepository.save(user);
    }

    public String uploadAvatar(String username, MultipartFile file) throws IOException {
        User user = getCurrentUser(username);
        String filePath = fileService.storeFile(file, false);
        user.setAvatarUrl(filePath);
        userRepository.save(user);
        return filePath;
    }
}
