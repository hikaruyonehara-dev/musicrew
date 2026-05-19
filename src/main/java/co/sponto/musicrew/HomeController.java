package co.sponto.musicrew;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails user) {
        return user != null ? "redirect:/home" : "index";
    }
}
