package com.campusfix.controller;

import com.campusfix.dto.Dtos.*;
import com.campusfix.model.User;
import com.campusfix.repository.UserRepository;
import com.campusfix.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    @Value("${app.admin-code}")
    private String adminCode;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.users = users;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    // POST /api/auth/register  (send adminCode matching app.admin-code to create an admin)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        if (isBlank(req.name()) || isBlank(req.email()) || isBlank(req.password()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name, email and password are required");
        if (req.password().length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");

        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");

        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setRollNo(req.rollNo());
        user.setPassword(encoder.encode(req.password()));
        boolean isAdmin = !isBlank(req.adminCode()) && req.adminCode().equals(adminCode);
        user.setRole(isAdmin ? "admin" : "student");
        users.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(jwtUtil.generateToken(user), UserResponse.of(user)));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest req) {
        String email = req.email() == null ? "" : req.email().trim().toLowerCase();
        User user = users.findByEmail(email)
                .filter(u -> req.password() != null && encoder.matches(req.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));
        return new AuthResponse(jwtUtil.generateToken(user), UserResponse.of(user));
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public Map<String, UserResponse> me(@AuthenticationPrincipal User user) {
        return Map.of("user", UserResponse.of(user));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
