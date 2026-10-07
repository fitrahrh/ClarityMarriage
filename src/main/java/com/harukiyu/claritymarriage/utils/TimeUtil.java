package com.harukiyu.claritymarriage.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting time, durations, and dates.
 */
public final class TimeUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private TimeUtil() {}

    /**
     * Formats an epoch millisecond timestamp to a readable date.
     */
    public static synchronized String formatDate(long millis) {
        return DATE_FORMAT.format(new Date(millis));
    }

    /**
     * Formats duration in seconds into a human-readable string (e.g., 2d 5h 30m).
     */
    public static String formatDuration(long totalSeconds) {
        if (totalSeconds < 60) {
            return totalSeconds + "s";
        }

        long days = TimeUnit.SECONDS.toDays(totalSeconds);
        long hours = TimeUnit.SECONDS.toHours(totalSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || (days == 0 && hours == 0)) {
            sb.append(minutes).append("m");
        }

        return sb.toString().trim();
    }

    /**
     * Formats milliseconds duration into seconds/minutes string.
     */
    public static String formatCooldown(long millisRemaining) {
        long seconds = (millisRemaining + 999) / 1000;
        return String.valueOf(seconds);
    }
}
