package co.sponto.musicrew.report;

import java.time.Instant;

import co.sponto.musicrew.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_id", nullable = false)
    private User reported;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(length = 1000)
    private String description;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("'OPEN'")
    private ReportStatus status = ReportStatus.OPEN;

    @Column(nullable = false)
    private Instant createdAt;

    @Setter
    private Instant reviewedAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    public Report(User reporter, User reported, ReportReason reason, String description) {
        this.reporter = reporter;
        this.reported = reported;
        this.reason = reason;
        this.description = description;
        this.status = ReportStatus.OPEN;
        this.createdAt = Instant.now();
    }
}
