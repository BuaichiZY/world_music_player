package dev.worldmusic;

/** One playback allowance per observed 6000-tick interval in the current connection. */
public final class PeriodPlayback {
    private Long interval;
    private boolean used;

    public boolean update(long clockTime) {
        long next = Math.floorDiv(clockTime, 6000L);
        if (interval != null && interval == next) return false;
        interval = next;
        used = false;
        return true;
    }

    public boolean used() { return used; }

    public void consume() { used = true; }

    public void reset() {
        interval = null;
        used = false;
    }
}
