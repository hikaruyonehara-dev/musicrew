package co.sponto.musicrew.messaging;

import java.time.Instant;

import co.sponto.musicrew.user.User;
import jakarta.persistence.*;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "user_a_id", "user_b_id" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Column(nullable = false)
    private Instant createdAt;

    public Conversation(User userA, User userB) {
        if (userA.getId().equals(userB.getId())) {
            throw new IllegalArgumentException("Cannot create conversation with self");
        }
        if (userA.getId() < userB.getId()) {
            this.userA = userA;
            this.userB = userB;
        } else {
            this.userA = userB;
            this.userB = userA;
        }
        this.createdAt = Instant.now();
    }

    public User otherParticipant(User me) {
        return userA.getId().equals(me.getId()) ? userB : userA;
    }

    public boolean includes(User user) {
        return userA.getId().equals(user.getId()) || userB.getId().equals(user.getId());
    }

}
