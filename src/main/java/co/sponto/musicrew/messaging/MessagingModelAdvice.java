package co.sponto.musicrew.messaging;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import co.sponto.musicrew.user.UserRepository;

/**
 * Adds messaging-related attributes to every Spring MVC model so that
 * layout.html and other shared templates can reference them without
 * every controller having to set them by hand.
 *
 * Currently exposes: {@code hasUnread} — true if the current authenticated
 * user has at least one unread message anywhere. Used to render the red
 * dot on the "Messages" nav link.
 */
@ControllerAdvice
public class MessagingModelAdvice {

    private final MessagingService messagingService;
    private final UserRepository userRepository;

    public MessagingModelAdvice(MessagingService messagingService, UserRepository userRepository) {
        this.messagingService = messagingService;
        this.userRepository = userRepository;
    }

    @ModelAttribute("hasUnread")
    public boolean hasUnread() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return false;
        }
        return userRepository.findByEmail(auth.getName().toLowerCase())
                .map(messagingService::hasUnread)
                .orElse(false);
    }
}
