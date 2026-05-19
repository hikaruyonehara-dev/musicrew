package co.sponto.musicrew.listing;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sponto.musicrew.profile.Country;
import co.sponto.musicrew.profile.Profile;
import co.sponto.musicrew.profile.ProfileService;
import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserService;

@Controller
public class ListingController {

    private final ListingService listingService;
    private final ProfileService profileService;
    private final UserService userService;

    public ListingController(ListingService listingService, ProfileService profileService,
            UserService userService) {
        this.listingService = listingService;
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("/home")
    public String feed(@RequestParam(required = false) List<Long> instrumentIds,
            @RequestParam(required = false) List<Long> genreIds, @RequestParam(required = false) Country country,
            @AuthenticationPrincipal UserDetails principal, Model model) {
        List<Listing> listings = listingService.feed(instrumentIds, genreIds, country);
        User me = userService.getByEmail(principal.getUsername());

        Map<Long, String> posterNames = new HashMap<>();
        Map<Long, String> posterAvatars = new HashMap<>();
        for (Listing l : listings) {
            Profile profile = profileService.getByUserId(l.getUser().getId());
            posterNames.put(l.getId(), profile.getDisplayName());
            posterAvatars.put(l.getId(), profile.getProfilePicPath());
        }

        model.addAttribute("me", me);
        model.addAttribute("listings", listings);
        model.addAttribute("posterNames", posterNames);
        model.addAttribute("posterAvatars", posterAvatars);

        model.addAttribute("allInstruments", profileService.allInstruments());
        model.addAttribute("allGenres", profileService.allGenres());
        model.addAttribute("allCountries", Country.values());
        model.addAttribute("selectedInstruments", instrumentIds == null ? List.of() : instrumentIds);
        model.addAttribute("selectedGenres", genreIds == null ? List.of() : genreIds);
        model.addAttribute("selectedCountry", country);

        return "listings/feed";
    }

    @GetMapping("/listings/new")
    public String newForm(Model model) {
        model.addAttribute("allInstruments", profileService.allInstruments());
        model.addAttribute("allGenres", profileService.allGenres());
        model.addAttribute("allCountries", Country.values());
        return "listings/new";
    }

    @PostMapping("/listings")
    public String create(@AuthenticationPrincipal UserDetails principal,
            @RequestParam String title, @RequestParam String description,
            @RequestParam(required = false) List<Long> instrumentIds,
            @RequestParam(required = false) List<Long> genreIds,
            @RequestParam(required = false) Country country,
            @RequestParam(required = false) String city, RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        Listing created = listingService.create(me, title, description, instrumentIds, genreIds, country, city);
        redirect.addFlashAttribute("flash", "Listing posted.");

        return "redirect:/listings/" + created.getId();
    }

    @GetMapping("/listings/{id}")
    public String view(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model) {
        Listing listing = listingService.getById(id);
        User me = userService.getByEmail(principal.getUsername());
        Profile posterProfile = profileService.getByUserId(listing.getUser().getId());

        model.addAttribute("listing", listing);
        model.addAttribute("posterProfile", posterProfile);
        model.addAttribute("isOwner", listing.getUser().getId().equals(me.getId()));

        return "listings/view";
    }

    @GetMapping("/listings/{id}/edit")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal, Model model) {
        Listing listing = listingService.getById(id);
        User me = userService.getByEmail(principal.getUsername());
        if (!listing.getUser().getId().equals(me.getId())) {
            return "redirect:/listings/" + id;
        }

        model.addAttribute("listing", listing);
        model.addAttribute("allInstruments", profileService.allInstruments());
        model.addAttribute("allGenres", profileService.allGenres());
        model.addAttribute("allCountries", Country.values());
        return "listings/edit";
    }

    @PostMapping("/listings/{id}/edit")
    public String update(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
            @RequestParam String title, @RequestParam String description,
            @RequestParam(required = false) List<Long> instrumentIds,
            @RequestParam(required = false) List<Long> genreIds, @RequestParam(required = false) Country country,
            @RequestParam(required = false) String city,
            RedirectAttributes redirect) {

        User me = userService.getByEmail(principal.getUsername());
        listingService.update(id, me.getId(), title, description, instrumentIds, genreIds, country, city);
        redirect.addFlashAttribute("flash", "Listing updated.");
        return "redirect:/listings/" + id;
    }

    @PostMapping("/listing/{id}/close")
    public String close(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        listingService.close(id, me.getId());
        redirect.addFlashAttribute("flash", "Listing closed.");
        return "redirect:/home";
    }

    @PostMapping("/listings/{id}/delete")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User me = userService.getByEmail(principal.getUsername());
        listingService.delete(id, me.getId());
        redirect.addFlashAttribute("flash", "Listing deleted.");
        return "redirect:/home";
    }
}
