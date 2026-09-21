package dev.worldmusic;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/** Dependency-free regression checks, run by Gradle check with Java 25. */
public final class MusicLogicTest {
    public static void main(String[] args) {
        check(TimePeriod.at(0) == TimePeriod.MORNING, "sunrise");
        check(TimePeriod.at(5999) == TimePeriod.MORNING, "morning end");
        check(TimePeriod.at(6000) == TimePeriod.NOON, "noon boundary");
        check(TimePeriod.at(11999) == TimePeriod.NOON, "noon end");
        check(TimePeriod.at(12000) == TimePeriod.EVENING, "sunset boundary");
        check(TimePeriod.at(17999) == TimePeriod.EVENING, "evening end");
        check(TimePeriod.at(18000) == TimePeriod.MIDNIGHT, "midnight boundary");
        check(TimePeriod.at(23999) == TimePeriod.MIDNIGHT, "midnight end");
        check(TimePeriod.at(24000) == TimePeriod.MORNING, "day wrap");
        check(TimePeriod.at(-1) == TimePeriod.MIDNIGHT, "negative time wrap");
        check(TimePeriod.at(24000L * 100000000 + 6000) == TimePeriod.NOON, "long-running world");
        var random = new Random(42);
        check(Playlist.choose(List.of(), null, random) == null, "empty playlist");
        check("one".equals(Playlist.choose(List.of("one"), "one", random)), "single track selection");
        check("one".equals(Playlist.choose(List.of("one", "one"), "one", random)), "duplicate tracks");
        var tracks = IntStream.range(0, 10).boxed().toList();
        var seen = new HashSet<Integer>();
        Integer previous = null;
        for (int i = 0; i < 10000; i++) {
            Integer selected = Playlist.choose(tracks, previous, random);
            check(!selected.equals(previous), "no immediate repeat");
            check(tracks.contains(selected), "selected track belongs to playlist");
            seen.add(selected);
            previous = selected;
        }
        check(seen.size() == 10, "all ten slots can play");
        var tooMany = IntStream.range(0, 11).boxed().toList();
        for (int i = 0; i < 1000; i++) check(Playlist.choose(tooMany, null, random) < 10, "ten-track cap");
        var playback = new PeriodPlayback();
        check(playback.update(0) && !playback.used(), "first interval allows a song");
        playback.consume();
        check(!playback.update(5999) && playback.used(), "same interval never replenishes playback");
        check(playback.update(6000) && !playback.used(), "new interval allows a song");
        playback.consume();
        check(playback.update(30000) && !playback.used(), "same time of day on another day allows playback");
        playback.consume();
        playback.reset();
        check(playback.update(30000) && !playback.used(), "new connection resets allowance");
        System.out.println("World Music: time boundaries, random selection, ten-track cap and one song per interval passed.");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
