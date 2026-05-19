package co.sponto.musicrew.report;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sponto.musicrew.profile.Profile;
import co.sponto.musicrew.profile.ProfileService;
import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserService;

@Controller
public class ReportController {

    private final ReportService reportService;
    private final UserService userService;
    private final ProfileService profileService;

    public ReportController(ReportService reportService,
            UserService userService,
            ProfileService profileService) {
        this.reportService = reportService;
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping("/users/{userId}/report")
    public String form(@PathVariable Long userId, @AuthenticationPrincipal UserDetails principal, Model model) {
        User me = userService.getByEmail(principal.getUsername());
        if (me.getId().equals(userId)) {
            return "redirect:/profile/" + userId;
        }
        Profile reportedProfile = profileService.getByUserId(userId);

        model.addAttribute("reportedProfile", reportedProfile);
        model.addAttribute("reasons", ReportReason.values());
        return "report/form";
    }

    @PostMapping("/users/{userId}/report")
    public String submit(@PathVariable Long userId,
            @RequestParam ReportReason reason,
            @RequestParam(required = false) String description,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        try {
            reportService.file(me, userId, reason, description);
            redirect.addFlashAttribute("flash", "Thanks for the report. Our team will review it.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/users/" + userId + "/report";
        }
        return "redirect:/profile/" + userId;
    }
}
