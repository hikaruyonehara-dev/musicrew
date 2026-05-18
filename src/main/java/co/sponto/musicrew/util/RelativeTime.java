package co.sponto.musicrew.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class RelativeTime {

    private static final DateTimeFormatter MONTH_DAY = DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter MONTH_DAY_YEAR = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter HOUR_MINUTE = DateTimeFormatter.ofPattern("HH:mm");

    private RelativeTime() {
    }

    public static String format(Instant instant) {
        if (instant == null)
            return "";
        long seconds = Duration.between(instant, Instant.now()).getSeconds();

        if (seconds < 60)
            return "just now";
        if (seconds < 3600)
            return (seconds / 60) + "m";
        if (seconds < 86400)
            return (seconds / 3600) + "h";
        if (seconds < 604800)
            return (seconds / 86400) + "d";

        ZoneId zone = ZoneId.systemDefault();
        LocalDate date = instant.atZone(zone).toLocalDate();
        LocalDate today = LocalDate.now(zone);
        if (date.getYear() == today.getYear()) {
            return MONTH_DAY.format(instant.atZone(zone));
        }
        return MONTH_DAY_YEAR.format(instant.atZone(zone));
    }

    public static String formatTime(Instant instant) {
        if (instant == null)
            return "";
        return HOUR_MINUTE.format(instant.atZone(ZoneId.systemDefault()));
    }

}
