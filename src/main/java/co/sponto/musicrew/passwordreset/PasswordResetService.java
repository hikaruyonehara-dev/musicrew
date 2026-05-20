package co.sponto.musicrew.passwordreset;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserRepository;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private static final int TOKEN_BYTES = 32; // 256 random bits
    private static final long EXPIRY_HOURS = 1;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Issues a reset token for the given email if a user exists.
     * Returns the full reset URL so the controller can display it (dev mode).
     * Returns Optional.empty() for unknown emails — caller still shows a generic
     * success message to avoid email enumeration.
     */
    @Transactional
    public Optional<String> requestReset(String email, String resetUrlBase) {
        return userRepository.findByEmail(email.toLowerCase()).map(user -> {
            String plaintext = generatePlaintextToken();
            String hash = sha256Hex(plaintext);
            Instant expiresAt = Instant.now().plus(EXPIRY_HOURS, ChronoUnit.HOURS);

            tokenRepository.save(new PasswordResetToken(user, hash, expiresAt));

            String url = resetUrlBase + "?token=" + plaintext;
            log.info("Password reset requested for {} — link: {}", user.getEmail(), url);
            return url;
        });
    }

    /**
     * Validates the plaintext token and returns the underlying user.
     * Throws IllegalArgumentException for missing, expired, or already-used tokens.
     */
    public User validateToken(String plaintext) {
        PasswordResetToken token = tokenRepository.findByTokenHash(sha256Hex(plaintext))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));
        if (!token.isValid()) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }
        return token.getUser();
    }

    /**
     * Consumes the token and updates the user's password.
     */
    @Transactional
    public void resetPassword(String plaintext, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        PasswordResetToken token = tokenRepository.findByTokenHash(sha256Hex(plaintext))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));
        if (!token.isValid()) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsedAt(Instant.now());
    }

    // --- helpers ---

    private String generatePlaintextToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
