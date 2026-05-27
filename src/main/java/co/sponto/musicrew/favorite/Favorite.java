package co.sponto.musicrew.favorite;

import java.time.Instant;

import co.sponto.musicrew.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite", uniqueConstraints = @UniqueConstraint(columnNames = { "favoriter_id", "favorited_id" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "favoriter_id", nullable = false)
    private User favoriter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "favorited_id", nullable = false)
    private User favorited;

    @Column(nullable = false)
    private Instant createdAt;

    public Favorite(User favoriter, User favorited) {
        this.favoriter = favoriter;
        this.favorited = favorited;
        this.createdAt = Instant.now();
    }
}
