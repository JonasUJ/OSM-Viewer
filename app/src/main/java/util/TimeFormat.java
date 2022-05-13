package util;

import java.time.Duration;

public class TimeFormat {
    private TimeFormat() {
    }

    // Credit: https://stackoverflow.com/a/65586659/7362723
    public static String formatDuration(Duration duration) {
        var s = duration.getSeconds();
        return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }
}
