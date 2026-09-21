package dev.worldmusic;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class Playlist {
    public static final int MAX_TRACKS = 10;

    private Playlist() {}

    /** Choose uniformly among distinct tracks, avoiding the previous track when possible. */
    public static <T> T choose(List<T> tracks, T previous, RandomGenerator random) {
        List<T> unique = tracks.stream().limit(MAX_TRACKS).distinct().toList();
        if (unique.isEmpty()) return null;
        List<T> candidates = unique.stream().filter(t -> !Objects.equals(t, previous)).toList();
        if (candidates.isEmpty()) candidates = unique;
        return candidates.get(random.nextInt(candidates.size()));
    }
}
