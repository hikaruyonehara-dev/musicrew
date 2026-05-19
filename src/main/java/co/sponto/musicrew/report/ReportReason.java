package co.sponto.musicrew.report;

public enum ReportReason {
    SPAM,
    HARASSMENT,
    INAPPROPRIATE_CONTENT,
    IMPERSONATION,
    OTHER;

    public String label() {
        return switch (this) {
            case SPAM -> "Spam";
            case HARASSMENT -> "Harassment or hateful behavior";
            case INAPPROPRIATE_CONTENT -> "Inappropriate content";
            case IMPERSONATION -> "Impersonation";
            case OTHER -> "Other";
        };
    }
}
