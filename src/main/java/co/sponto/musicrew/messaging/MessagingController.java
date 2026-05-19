package co.sponto.musicrew.messaging;

import co.sponto.musicrew.block.BlockService;
import co.sponto.musicrew.profile.Profile;
import co.sponto.musicrew.profile.ProfileService;
import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messages")
public class MessagingController {

    private final MessagingService messagingService;
    private final UserService userService;
    private final ProfileService profileService;
    private final MessageRepository messageRepository;
    private final BlockService blockService;

    public MessagingController(MessagingService messagingService,
            UserService userService,
            ProfileService profileService,
            MessageRepository messageRepository,
            BlockService blockService) {
        this.messagingService = messagingService;
        this.userService = userService;
        this.profileService = profileService;
        this.messageRepository = messageRepository;
        this.blockService = blockService;
    }

    @GetMapping
    public String inbox(@AuthenticationPrincipal UserDetails principal, Model model) {
        User me = userService.getByEmail(principal.getUsername());
        List<Conversation> conversations = messagingService.inbox(me);

        Map<Long, Long> unreadCounts = new HashMap<>();
        Map<Long, String> displayNames = new HashMap<>();
        Map<Long, Instant> lastTimes = new HashMap<>();

        for (Conversation c : conversations) {
            User other = c.otherParticipant(me);
            Profile otherProfile = profileService.getByUserId(other.getId());

            unreadCounts.put(c.getId(), messagingService.unreadCount(c.getId(), me));
            displayNames.put(c.getId(), otherProfile.getDisplayName());

            Instant lastTime = messageRepository
                    .findFirstByConversationIdOrderBySentAtDesc(c.getId())
                    .map(Message::getSentAt)
                    .orElse(c.getCreatedAt());
            lastTimes.put(c.getId(), lastTime);
        }

        model.addAttribute("me", me);
        model.addAttribute("conversations", conversations);
        model.addAttribute("unreadCounts", unreadCounts);
        model.addAttribute("displayNames", displayNames);
        model.addAttribute("lastTimes", lastTimes);
        return "messaging/inbox";
    }

    @GetMapping("/{id}")
    public String viewConversation(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {
        User me = userService.getByEmail(principal.getUsername());
        Conversation c = messagingService.getConversationFor(me, id);
        messagingService.markConversationRead(id, me);

        User other = c.otherParticipant(me);
        Profile otherProfile = profileService.getByUserId(other.getId());

        model.addAttribute("me", me);
        model.addAttribute("conversation", c);
        model.addAttribute("other", other);
        model.addAttribute("otherProfile", otherProfile);
        model.addAttribute("messages", messagingService.messages(id));
        model.addAttribute("isBlocked", blockService.isBlockedBetween(me.getId(), other.getId()));

        return "messaging/conversation";
    }

    @PostMapping("/{id}")
    public String sendMessage(@PathVariable Long id,
            @RequestParam String body,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        try {
            messagingService.send(me, id, body);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/messages/" + id;
    }

    @PostMapping("/start/{userId}")
    public String startConversation(@PathVariable Long userId,
            @AuthenticationPrincipal UserDetails principal) {
        User me = userService.getByEmail(principal.getUsername());
        Conversation c = messagingService.findOrCreateConversation(me, userId);
        return "redirect:/messages/" + c.getId();
    }

    @PostMapping("/{id}/delete")
    public String deleteConversation(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        messagingService.deleteConversation(me, id);
        redirect.addFlashAttribute("flash", "Conversation deleted.");
        return "redirect:/messages";
    }
}
