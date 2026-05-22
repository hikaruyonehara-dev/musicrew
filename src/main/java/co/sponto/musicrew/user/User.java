package co.sponto.musicrew.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private Instant createdAt;

    @Setter
    @Column(nullable = false)
    private boolean enabled = true;

    @Setter
    private Instant lastSeenAt;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("'USER'")
    private Role role = Role.USER;

    public User(String email, String passwordHash, LocalDate dateOfBirth) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.dateOfBirth = dateOfBirth;
        this.createdAt = Instant.now();
    }

    private static final long ONLINE_WINDOW_MINUTES = 5;

    public boolean isOnline() {
        if (lastSeenAt == null) {
            return false;
        }
        return java.time.Duration.between(lastSeenAt, java.time.Instant.now())
                .toMinutes() < ONLINE_WINDOW_MINUTES;
    }
}
