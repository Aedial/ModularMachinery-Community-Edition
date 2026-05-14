package github.kasuminova.mmce.common.util;


public final class PollingRateUtils {

    public static final int TICKS_PER_TICK = 1;
    public static final int TICKS_PER_SECOND = 20;
    public static final int TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
    public static final int TICKS_PER_HOUR = TICKS_PER_MINUTE * 60;
    public static final int TICKS_PER_DAY = TICKS_PER_HOUR * 24;

    private PollingRateUtils() {
    }

    public static String format(long ticks) {
        if (ticks <= 0) return "0";

        StringBuilder builder = new StringBuilder();

        if (ticks >= TICKS_PER_DAY) {
            long days = ticks / TICKS_PER_DAY;
            builder.append(days).append('d').append(' ');
            ticks %= TICKS_PER_DAY;
        }

        if (ticks >= TICKS_PER_HOUR) {
            long hours = ticks / TICKS_PER_HOUR;
            builder.append(hours).append('h').append(' ');
            ticks %= TICKS_PER_HOUR;
        }

        if (ticks >= TICKS_PER_MINUTE) {
            long minutes = ticks / TICKS_PER_MINUTE;
            builder.append(minutes).append('m').append(' ');
            ticks %= TICKS_PER_MINUTE;
        }

        if (ticks >= TICKS_PER_SECOND) {
            long seconds = ticks / TICKS_PER_SECOND;
            builder.append(seconds).append('s').append(' ');
            ticks %= TICKS_PER_SECOND;
        }

        if (ticks > 0) builder.append(ticks).append('t');

        return builder.toString().trim();
    }
}