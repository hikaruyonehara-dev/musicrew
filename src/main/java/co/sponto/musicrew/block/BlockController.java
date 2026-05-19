package co.sponto.musicrew.block;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserService;

@Controller
public class BlockController {
    private final BlockService blockService;
    private final UserService userService;

    public BlockController(BlockService blockService, UserService userService) {
        this.blockService = blockService;
        this.userService = userService;
    }

    @PostMapping("/users/{userId}/block")
    public String block(@PathVariable Long userId, @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        blockService.block(me, userId);
        redirect.addFlashAttribute("flash", "User blocked.");
        return "redirect:/home";
    }

    @PostMapping("/users/{userId}/unblock")
    public String unblock(@PathVariable Long userId, @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        blockService.unblock(me, userId);
        redirect.addFlashAttribute("flash", "User unblocked.");
        return "redirect:/profile/me/blocks";
    }
}
