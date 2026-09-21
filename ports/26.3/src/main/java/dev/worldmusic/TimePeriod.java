package dev.worldmusic;

/** Minecraft day ticks: 0 = 06:00, 6000 = 12:00, 12000 = 18:00, 18000 = 00:00. */
public enum TimePeriod {
    MORNING, NOON, EVENING, MIDNIGHT;

    public static TimePeriod at(long dayTime) {
        return values()[(int) (Math.floorMod(dayTime, 24000L) / 6000L)];
    }

    public String key() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
