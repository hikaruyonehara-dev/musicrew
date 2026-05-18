package co.sponto.musicrew.user;

import co.sponto.musicrew.profile.SkillBadge;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signup")
    public String showSignup(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new SignupForm());
        }
        model.addAttribute("skillBadges", SkillBadge.values());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String doSignup(@Valid @ModelAttribute("form") SignupForm form,
                           BindingResult binding,
                           Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("skillBadges", SkillBadge.values());
            return "auth/signup";
        }
        try {
            userService.signUp(form.getEmail(), form.getPassword(),
                               form.getDateOfBirth(), form.getDisplayName(),
                               form.getSkillBadge());
        } catch (UserService.SignupException e) {
            binding.reject("signup.failed", e.getMessage());
            model.addAttribute("skillBadges", SkillBadge.values());
            return "auth/signup";
        }
        return "redirect:/login?signedUp";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "auth/login";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class SignupForm {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
        private String password;

        @NotBlank
        @Size(max = 60)
        private String displayName;

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dateOfBirth;

        @NotNull
        private SkillBadge skillBadge;
    }
}
