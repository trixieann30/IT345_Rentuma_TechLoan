package edu.cit.rentuma.techloan.features.auth;

import edu.cit.rentuma.techloan.features.auth.dto.*;
import edu.cit.rentuma.techloan.features.auth.model.RefreshToken;
import edu.cit.rentuma.techloan.features.auth.model.User;
import edu.cit.rentuma.techloan.features.auth.observer.AuthEventPublisher;
import edu.cit.rentuma.techloan.features.auth.repository.RefreshTokenRepository;
import edu.cit.rentuma.techloan.features.auth.repository.UserRepository;
import edu.cit.rentuma.techloan.features.auth.validator.RegistrationValidator;
import edu.cit.rentuma.techloan.shared.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RegistrationValidator validator;
    private final AuthEventPublisher authEventPublisher;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RegistrationValidator validator,
                       AuthEventPublisher authEventPublisher) {
        this.userRepository         = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder        = passwordEncoder;
        this.jwtUtil                = jwtUtil;
        this.validator              = validator;
        this.authEventPublisher     = authEventPublisher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validator.validate(request);

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(hashedPassword)
                .studentId(request.getStudentId())
                .role(request.getRole())
                .penaltyPoints(0)
                .build();

        User savedUser = userRepository.save(user);
        authEventPublisher.publishRegisterSuccess(savedUser);

        String token        = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getEmail());
        persistRefreshToken(savedUser.getId(), refreshToken);

        return AuthResponse.builder()
                .success(true)
                .user(UserResponse.from(savedUser))
                .token(token)
                .refreshToken(refreshToken)
                .message("Account created successfully. Welcome to TechLoan!")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() ->
                        new BadCredentialsException("AUTH-001:Invalid email or password"));

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("AUTH-001:Invalid email or password");
        }

        String token        = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        persistRefreshToken(user.getId(), refreshToken);

        return AuthResponse.builder()
                .success(true)
                .user(UserResponse.from(user))
                .token(token)
                .refreshToken(refreshToken)
                .message("Welcome back, " + user.getFullName() + "!")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("DB-001:User not found"));
        return UserResponse.from(user);
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user ->
                refreshTokenRepository.deleteByUserId(user.getId()));
    }

    private void persistRefreshToken(Long userId, String token) {
        refreshTokenRepository.deleteByUserId(userId);
        RefreshToken rt = new RefreshToken(userId, token, LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(rt);
    }
}
