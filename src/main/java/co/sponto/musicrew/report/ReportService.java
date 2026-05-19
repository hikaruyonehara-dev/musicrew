package co.sponto.musicrew.report;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.sponto.musicrew.user.User;
import co.sponto.musicrew.user.UserRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Report file(User reporter, Long reportedUserId, ReportReason reason, String description) {
        if (reason == null) {
            throw new IllegalArgumentException("Pick a reason");
        }
        if (reporter.getId().equals(reportedUserId)) {
            throw new IllegalArgumentException("You can't report yourself");
        }
        User reported = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + reportedUserId));

        String trimmedDescription = (description == null || description.isBlank())
                ? null
                : description.trim();

        return reportRepository.save(new Report(reporter, reported, reason, trimmedDescription));
    }

    public List<Report> listByStatus(ReportStatus status) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public long countOpen() {
        return reportRepository.countByStatus(ReportStatus.OPEN);
    }

    @Transactional
    public void markReviewed(Long reportId, User admin) {
        updateStatus(reportId, admin, ReportStatus.REVIEWED);
    }

    @Transactional
    public void dismiss(Long reportId, User admin) {
        updateStatus(reportId, admin, ReportStatus.DISMISSED);
    }

    private void updateStatus(Long reportId, User admin, ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        report.setStatus(newStatus);
        report.setReviewedAt(Instant.now());
        report.setReviewedBy(admin);
    }
}
