package edu.cit.rentuma.techloan.features.auth;

import edu.cit.rentuma.techloan.features.auth.dto.*;
import edu.cit.rentuma.techloan.features.auth.email.EmailService;
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
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RegistrationValidator validator;
    private final AuthEventPublisher authEventPublisher;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RegistrationValidator validator,
                       AuthEventPublisher authEventPublisher,
                       EmailService emailService) {
        this.userRepository         = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder        = passwordEncoder;
        this.jwtUtil                = jwtUtil;
        this.validator              = validator;
        this.authEventPublisher     = authEventPublisher;
        this.emailService           = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validator.validate(request);

        boolean isCustodian = request.getRole() == User.Role.CUSTODIAN;
        String verificationToken = isCustodian ? null : UUID.randomUUID().toString();

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .studentId(request.getStudentId())
                .role(request.getRole())
                .penaltyPoints(0)
                .emailVerified(isCustodian)
                .verificationToken(verificationToken)
                .build();

        User savedUser = userRepository.save(user);
        authEventPublisher.publishRegisterSuccess(savedUser);

        if (isCustodian) {
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

        return AuthResponse.builder()
                .success(true)
                .user(UserResponse.from(savedUser))
                .message("Account created! Please check your email to verify your account before logging in.")
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

        if (user.getRole() != User.Role.CUSTODIAN && !user.isEmailVerified()) {
            throw new IllegalStateException("AUTH-003:Please verify your email before logging in. Check your inbox for the verification link.");
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

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("VALID-004:Invalid or expired verification link"));
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("DB-001:User not found"));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("DB-001:User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank())
            user.setFullName(request.getFullName().trim());

        if (request.getStudentId() != null) {
            String sid = request.getStudentId().isBlank() ? null : request.getStudentId().trim();
            if (sid != null && !sid.equals(user.getStudentId()) && userRepository.existsByStudentId(sid))
                throw new IllegalArgumentException("PROFILE-001:This student ID is already in use");
            user.setStudentId(sid);
        }

        if (request.getPersonalEmail() != null)
            user.setPersonalEmail(request.getPersonalEmail().isBlank() ? null : request.getPersonalEmail().trim());

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank())
                throw new IllegalArgumentException("PROFILE-002:Current password is required to set a new one");
            if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))
                throw new IllegalArgumentException("PROFILE-003:Current password is incorrect");
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user ->
                refreshTokenRepository.deleteByUserId(user.getId()));
    }

    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("VALID-005:Invalid or expired reset link."));
        if (user.getPasswordResetExpiry() == null || user.getPasswordResetExpiry().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("VALID-005:Invalid or expired reset link.");
        if (newPassword == null || newPassword.length() < 6)
            throw new IllegalArgumentException("VALID-001:Password must be at least 6 characters.");
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
    }

    private void persistRefreshToken(Long userId, String token) {
        refreshTokenRepository.deleteByUserId(userId);
        RefreshToken rt = new RefreshToken(userId, token, LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(rt);
    }
}
