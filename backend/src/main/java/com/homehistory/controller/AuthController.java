package com.homehistory.controller;

import com.homehistory.model.User;
import com.homehistory.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/avatar")
    public ResponseEntity<Resource> getAvatar(Authentication auth) {
        try {
            String username = auth.getName();
            User user = authService.getCurrentUser(username);
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Path basePath = getBaseStoragePath();
            Path resolved = basePath.resolve(avatarUrl).normalize();
            Resource resource = new UrlResource(resolved.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header("Cache-Control", "public, max-age=86400")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Path getBaseStoragePath() {
        String storagePath = System.getProperty("app.storage.path", "./storage");
        Path base = Paths.get(storagePath);
        if (!base.isAbsolute()) {
            base = Paths.get(System.getProperty("user.home"), "home-history-data").toAbsolutePath();
        }
        return base;
    }
}
