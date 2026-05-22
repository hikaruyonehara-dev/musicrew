package co.sponto.musicrew.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LastSeenInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public LastSeenInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return true;
        }

        if ("anonumousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return true;
        }

        userService.touchLastSeen(auth.getName());
        return true;
    }

}
