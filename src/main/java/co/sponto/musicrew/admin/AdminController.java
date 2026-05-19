package co.sponto.musicrew.admin;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import co.sponto.musicrew.report.Report;
import co.sponto.musicrew.report.ReportService;
import co.sponto.musicrew.report.ReportStatus;
import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserService;

@Controller
public class AdminController {

    private final ReportService reportService;
    private final UserService userService;

    public AdminController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    @GetMapping("/admin/reports")
    public String reports(@RequestParam(required = false, defaultValue = "OPEN") ReportStatus status,
            Model model) {
        List<Report> reports = reportService.listByStatus(status);

        model.addAttribute("reports", reports);
        model.addAttribute("status", status);
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("openCount", reportService.countOpen());
        return "admin/reports";
    }

    @PostMapping("/admin/reports/{id}/review")
    public String review(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User admin = userService.getByEmail(principal.getUsername());
        reportService.markReviewed(id, admin);
        redirect.addFlashAttribute("flash", "Report marked as reviewed.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/admin/reports/{id}/dismiss")
    public String dismiss(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal,
            RedirectAttributes redirect) {
        User admin = userService.getByEmail(principal.getUsername());
        reportService.dismiss(id, admin);
        redirect.addFlashAttribute("flash", "Report dismissed.");
        return "redirect:/admin/reports";
    }
}
