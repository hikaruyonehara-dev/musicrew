package co.sponto.musicrew.messaging;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    long countByConversationIdAndSenderIdNotAndReadAtIsNull(Long conversationId, Long recipientId);

    Optional<Message> findFirstByConversationIdOrderBySentAtDesc(Long conversationId);
}
