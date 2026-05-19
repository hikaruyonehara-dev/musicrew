package co.sponto.musicrew.profile;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sponto.musicrew.block.BlockService;
import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;
    private final BlockService blockService;

    public ProfileController(ProfileService profileService, UserService userService, BlockService blockService) {
        this.profileService = profileService;
        this.userService = userService;
        this.blockService = blockService;
    }

    @GetMapping("/profile/me")
    public String myProfile(@AuthenticationPrincipal UserDetails principal, Model model) {

        Profile profile = profileService.getByUserEmail(principal.getUsername());
        model.addAttribute("profile", profile);
        model.addAttribute("isSelf", true);
        return "profile/view";
    }

    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model) {
        Profile profile = profileService.getById(id);
        Profile me = profileService.getByUserEmail(principal.getUsername());
        boolean isSelf = profile.getId().equals(me.getId());

        boolean blocked = !isSelf && blockService.isBlockedBetween(
                me.getUser().getId(), profile.getUser().getId());

        if (!isSelf && (blocked || profile.isHidden() || !profile.getUser().isEnabled())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("profile", profile);
        model.addAttribute("isSelf", isSelf);
        return "profile/view";
    }

    @GetMapping("/profile/me/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        Profile profile = profileService.getByUserEmail(principal.getUsername());
        model.addAttribute("profile", profile);
        model.addAttribute("allInstruments", profileService.allInstruments());
        model.addAttribute("allGenres", profileService.allGenres());
        model.addAttribute("allCountries", Country.values());
        model.addAttribute("skillBadges", SkillBadge.values());
        return "profile/edit";
    }

    @GetMapping("/profile/me/blocks")
    public String myBlocks(@AuthenticationPrincipal UserDetails principal, Model model) {
        Profile profile = profileService.getByUserEmail(principal.getUsername());
        model.addAttribute("myBlocks", blockService.myBlocks(profile.getUser()));
        return "profile/blocks";
    }

    @PostMapping("/profile/me/edit")
    public String doEdit(@AuthenticationPrincipal UserDetails principal, @RequestParam String displayName,
            @RequestParam(required = false) String bio, @RequestParam(required = false) Country country,
            @RequestParam(required = false) String city, @RequestParam SkillBadge skillBadge,
            @RequestParam(required = false) List<Long> instrumentIds,
            @RequestParam(required = false) List<Long> genreIds,
            RedirectAttributes redirect) {

        Profile profile = profileService.getByUserEmail(principal.getUsername());
        profileService.update(profile.getId(), displayName, bio, country, city, skillBadge, instrumentIds, genreIds);
        redirect.addFlashAttribute("flash", "Profile updated");
        return "redirect:/profile/me";
    }

    @PostMapping("/profile/me/picture")
    public String uploadPicture(@AuthenticationPrincipal UserDetails principal,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect) {
        Profile profile = profileService.getByUserEmail(principal.getUsername());
        try {
            profileService.uploadPicture(profile.getId(), file);
            redirect.addFlashAttribute("flash", "Profile picture updated.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/me/edit";
    }

    @PostMapping("/profile/me/videos")
    public String addVideo(@AuthenticationPrincipal UserDetails principal,
            @RequestParam String url, RedirectAttributes redirect) {
        Profile profile = profileService.getByUserEmail(principal.getUsername());
        try {
            profileService.addVideoLink(profile.getId(), url);
            redirect.addFlashAttribute("flash", "Video link added.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/me/edit";
    }

    @PostMapping("/profile/me/videos/{id}/delete")
    public String removeVideo(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            RedirectAttributes redirect) {
        Profile profile = profileService.getByUserEmail(principal.getUsername());
        profileService.removeVideoLink(profile.getId(), id);
        redirect.addFlashAttribute("flash", "Video link removed.");
        return "redirect:/profile/me/edit";
    }

    @PostMapping("/profile/me/music")
    public String addMusic(@AuthenticationPrincipal UserDetails principal,
            @RequestParam String url,
            RedirectAttributes redirect) {
        Profile profile = profileService.getByUserEmail(principal.getUsername());
        try {
            profileService.addMusicLink(profile.getId(), url);
            redirect.addFlashAttribute("flash", "Music link added.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/me/edit";
    }

    @PostMapping("/profile/me/music/{id}/delete")
    public String removeMusic(@AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            RedirectAttributes redirect) {
        Profile profile = profileService.getByUserEmail(principal.getUsername());
        profileService.removeMusicLink(profile.getId(), id);
        redirect.addFlashAttribute("flash", "Music link removed.");
        return "redirect:/profile/me/edit";
    }

    @PostMapping("/profile/me/hide")
    public String toggleHidden(@AuthenticationPrincipal UserDetails principal, RedirectAttributes redirect) {

        Profile profile = profileService.getByUserEmail(principal.getUsername());
        profileService.toggleHidden(profile.getId());

        String message = !profile.isHidden()
                ? "Your profile is now visible again."
                : "Your profile is now hidden from others.";
        redirect.addFlashAttribute("flash", message);
        return "redirect:/profile/me";
    }

    @PostMapping("/account/delete")
    public String deleteAccount(@AuthenticationPrincipal UserDetails principal, HttpServletRequest request)
            throws ServletException {

        User me = userService.getByEmail(principal.getUsername());
        profileService.deleteAccount(me.getId());

        request.logout();
        return "redirect:/";
    }

}
