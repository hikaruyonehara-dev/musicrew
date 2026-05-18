package co.sponto.musicrew.messaging;

import co.sponto.musicrew.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c " +
            "WHERE c.userA.id = :userAId AND c.userB.id = :userBId")
    Optional<Conversation> findByPair(@Param("userAId") Long userAId,
            @Param("userBId") Long userBId);

    @Query("SELECT c FROM Conversation c " +
            "WHERE c.userA = :user OR c.userB = :user " +
            "ORDER BY c.createdAt DESC")
    List<Conversation> findAllForUser(@Param("user") User user);
}
