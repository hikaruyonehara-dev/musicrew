package co.sponto.musicrew.messaging;

import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessagingService(ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Conversation findOrCreateConversation(User me, Long otherUserId) {
        if (me.getId().equals(otherUserId)) {
            throw new IllegalArgumentException("Cannot message yourself");
        }
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("Other user not found"));

        Long a = Math.min(me.getId(), other.getId());
        Long b = Math.max(me.getId(), other.getId());

        return conversationRepository.findByPair(a, b)
                .orElseGet(() -> conversationRepository.save(new Conversation(me, other)));
    }

    public List<Conversation> inbox(User me) {
        return conversationRepository.findAllForUser(me);
    }

    public Conversation getConversationFor(User me, Long conversationId) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        if (!c.includes(me)) {
            throw new SecurityException("You are not a participant of this conversation");
        }
        return c;
    }

    public List<Message> messages(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

    public long unreadCount(Long conversationId, User me) {
        return messageRepository.countByConversationIdAndSenderIdNotAndReadAtIsNull(
                conversationId, me.getId());
    }

    @Transactional
    public Message send(User me, Long conversationId, String body) {
        Conversation c = getConversationFor(me, conversationId);
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        return messageRepository.save(new Message(c, me, body.trim()));
    }

    @Transactional
    public void markConversationRead(Long conversationId, User me) {
        for (Message m : messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)) {
            if (!m.getSender().getId().equals(me.getId())) {
                m.markRead();
            }
        }
    }
}
