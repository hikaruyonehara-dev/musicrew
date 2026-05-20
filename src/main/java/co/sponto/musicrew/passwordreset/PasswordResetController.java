package co.sponto.musicrew.passwordreset;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotSubmit(@RequestParam String email, RedirectAttributes redirect) {
        String resetUrlBase = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/reset-password")
                .toUriString();

        passwordResetService.requestReset(email, resetUrlBase)
                .ifPresent(url -> redirect.addFlashAttribute("resetUrl", url));

        redirect.addFlashAttribute("flash",
                "If an account exists for that email, we've sent a reset link.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam String token, Model model, RedirectAttributes redirect) {
        try {
            passwordResetService.validateToken(token);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage() + " — request a new one below.");
            return "redirect:/forgot-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(@RequestParam String token, @RequestParam String password,
            RedirectAttributes redirect) {
        try {
            passwordResetService.resetPassword(token, password);
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addAttribute("token", token); // keep ?token=… on redirect
            return "redirect:/reset-password";
        }
        redirect.addFlashAttribute("flash", "Password updated. You can now log in.");
        return "redirect:/login";
    }
}
