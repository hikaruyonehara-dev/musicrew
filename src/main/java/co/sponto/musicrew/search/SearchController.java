package co.sponto.musicrew.search;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import co.sponto.musicrew.profile.Country;
import co.sponto.musicrew.profile.Profile;
import co.sponto.musicrew.profile.ProfileService;

@Controller
public class SearchController {
    private final SearchService searchService;
    private final ProfileService profileService;
    private final UserService userService;

    public SearchController(SearchService searchService,
            ProfileService profileService,
            UserService userService) {
        this.searchService = searchService;
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) List<Long> instrumentIds,
            @RequestParam(required = false) List<Long> genreIds,
            @RequestParam(required = false) Country country,
            @RequestParam(required = false) String city,
            @AuthenticationPrincipal UserDetails principal,
            Model model) {

        User me = userService.getByEmail(principal.getUsername());
        List<Profile> results = searchService.search(instrumentIds, genreIds, country, city, me.getId());

        model.addAttribute("results", results);
        model.addAttribute("allInstruments", profileService.allInstruments());
        model.addAttribute("allGenres", profileService.allGenres());
        model.addAttribute("allCountries", Country.values());

        model.addAttribute("selectedInstruments", instrumentIds == null ? List.of() : instrumentIds);
        model.addAttribute("selectedGenres", genreIds == null ? List.of() : genreIds);
        model.addAttribute("selectedCountry", country);
        model.addAttribute("selectedCity", city);

        return "search/search";
    }

}
