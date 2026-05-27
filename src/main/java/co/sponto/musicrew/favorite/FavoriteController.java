package co.sponto.musicrew.favorite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sponto.musicrew.profile.Profile;
import co.sponto.musicrew.profile.ProfileService;
import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserService;

@Controller
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final UserService userService;
    private final ProfileService profileService;

    public FavoriteController(FavoriteService favoriteService, UserService userService, ProfileService profileService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
        this.profileService = profileService;
    }

    @PostMapping("/users/{userId}/favorite")
    public String favorite(@PathVariable Long userId, @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        favoriteService.favorite(me, userId);
        redirect.addFlashAttribute("flash", "User favorited");

        return "redirect:/profile/" + userId;
    }

    @PostMapping("/users/{userId}/unfavorite")
    public String unfavorite(@PathVariable Long userId, @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        favoriteService.unfavorite(me, userId);
        redirect.addFlashAttribute("flash", "Removed from favorite");

        return "redirect:/profile/me/favorites";
    }

    @GetMapping("/profile/me/favorites")
    public String checkFavorites(@AuthenticationPrincipal UserDetails principal, Model model) {
        User me = userService.getByEmail(principal.getUsername());
        List<Favorite> favList = favoriteService.myFavorites(me);

        Map<Long, Profile> profiles = new HashMap<>();
        for (Favorite f : favList) {
            Long userId = f.getFavorited().getId();
            profiles.put(userId, profileService.getByUserId(userId));
        }

        model.addAttribute("myFavorites", favList);
        model.addAttribute("favoriteProfiles", profiles);

        return "profile/favorites";
    }
}
