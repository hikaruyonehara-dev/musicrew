package co.sponto.musicrew.user;

import co.sponto.musicrew.profile.Profile;
import co.sponto.musicrew.profile.ProfileRepository;
import co.sponto.musicrew.profile.SkillBadge;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
public class UserService {

    private static final int MINIMUM_AGE = 18;

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User signUp(String email, String rawPassword, LocalDate dateOfBirth, String displayName,
            SkillBadge skillBadge) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new SignupException("Email is already registered.");
        }

        if (Period.between(dateOfBirth, LocalDate.now()).getYears() < MINIMUM_AGE) {
            throw new SignupException("You must be at least " + MINIMUM_AGE + " years old to sign up.");
        }

        User user = new User(normalizedEmail, passwordEncoder.encode(rawPassword), dateOfBirth);
        userRepository.save(user);

        Profile profile = new Profile(user, displayName.trim(), skillBadge);
        profileRepository.save(profile);

        return user;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    public static class SignupException extends RuntimeException {
        public SignupException(String msg) {
            super(msg);
        }
    }

}
