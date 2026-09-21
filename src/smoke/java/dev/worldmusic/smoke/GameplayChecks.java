package dev.worldmusic.smoke;

import com.mojang.logging.LogUtils;
import dev.worldmusic.TimePeriod;
import dev.worldmusic.WorldMusicConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Real integrated-world playback checks; this entire source set is excluded from release jars. */
final class GameplayChecks {
    private static final String[] TRACKS = {"minecraft", "living_mice", "danny", "dry_hands"};
    private final long deadline = System.currentTimeMillis() + 120_000;
    private boolean started;
    private int phase = -1;
    private int ticks;
    private SoundInstance latest;
    private SoundInstance stopped;
    private int musicStarts;
    private int beforeShortSong;

    GameplayChecks() {
        NeoForge.EVENT_BUS.addListener((PlaySoundEvent event) -> {
            if (event.getOriginalSound().getSource() == SoundSource.MUSIC) {
                musicStarts++;
                if (event.getOriginalSound().getIdentifier().getNamespace().startsWith("world_music")) {
                    latest = event.getOriginalSound();
                }
            }
        });
        NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        try {
            if (System.currentTimeMillis() > deadline) throw new AssertionError("Gameplay check timeout, phase " + phase);
            if (!started) {
                started = true;
                mc.options.pauseOnLostFocus = false;
                mc.options.renderDistance().set(2);
                mc.options.simulationDistance().set(5);
                mc.options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(0.05);
                for (TimePeriod period : TimePeriod.values()) {
                    WorldMusicConfig.TRACKS.get(period).set(List.of("world_music:vanilla." + TRACKS[period.ordinal()]));
                }
                mc.createWorldOpenFlows().createFreshLevel("playback-" + System.currentTimeMillis(),
                        new LevelSettings("World Music Playback Checks", GameType.CREATIVE,
                                new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT),
                        new WorldOptions(42, false, false), WorldPresets::createNormalWorldDimensions, null);
                return;
            }
            if (mc.level == null || mc.player == null || mc.getOverlay() != null || mc.screen != null) return;
            if (phase == -1) {
                phase = 0;
                setTime(0);
                return;
            }
            if (++ticks < 60) return;
            if (phase <= 4) {
                String expected = "world_music:vanilla." + TRACKS[phase % 4];
                check(latest != null && latest.getIdentifier().toString().equals(expected), "Scheduled playback at phase " + phase + ", expected " + expected);
                check(mc.getSoundManager().isActive(latest), "Audio channel active at phase " + phase);
                if (stopped != null) check(!mc.getSoundManager().isActive(stopped), "Previous period stopped");
                LogUtils.getLogger().info("WORLD_MUSIC_GAMEPLAY: phase {} sound {} active", phase, expected);
                stopped = latest;
                latest = null;
                phase++;
                ticks = 0;
                if (phase <= 4) setTime(phase * 6000);
                else mc.getSoundManager().stop(stopped);
            } else if (phase == 5) {
                check(latest == null && !mc.getSoundManager().isActive(stopped), "Stopped song does not restart in same interval");
                WorldMusicConfig.TRACKS.get(TimePeriod.NOON).set(List.of("world_music_smoke:short_1", "world_music_smoke:short_2"));
                beforeShortSong = musicStarts;
                setTime(30000);
                phase++;
                ticks = 0;
            } else if (phase == 6) {
                check(latest != null && latest.getIdentifier().getNamespace().equals("world_music_smoke"), "One short song selected from two candidates");
                check(!mc.getSoundManager().isActive(latest), "Short song ended naturally");
                check(musicStarts == beforeShortSong + 1, "No second song or vanilla fallback after natural completion");
                WorldMusicConfig.TRACKS.get(TimePeriod.NOON).set(List.of());
                WorldMusicConfig.EMPTY_USES_VANILLA.set(true);
                latest = null;
                phase++;
                ticks = 0;
            } else if (phase == 7) {
                check(latest == null && musicStarts == beforeShortSong + 1, "Changing to empty playlist does not restore music after playback");
                WorldMusicConfig.TRACKS.get(TimePeriod.NOON).set(List.of("world_music_smoke:short_1"));
                phase++;
                ticks = 0;
            } else if (phase == 8) {
                check(latest == null && musicStarts == beforeShortSong + 1, "Editing playlist does not replenish playback allowance");
                setTime(36000);
                phase++;
                ticks = 0;
            } else {
                check(latest != null && latest.getIdentifier().toString().equals("world_music:vanilla.danny")
                        && mc.getSoundManager().isActive(latest), "Next interval resumes scheduled playback");
                finish("PASS: Four periods and day rollover play correctly; stopped songs do not restart; one of two short songs plays and ends naturally; no second track or vanilla music follows; editing playlists preserves silence; next interval resumes playback.\n");
            }
        } catch (Throwable error) {
            LogUtils.getLogger().error("WORLD_MUSIC_GAMEPLAY_FAIL", error);
            finish("FAIL: " + error);
        }
    }

    private void setTime(int time) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), "time set " + time));
    }

    private void finish(String result) {
        try { Files.writeString(Path.of("gameplay-result.txt"), result); }
        catch (Exception error) { LogUtils.getLogger().error("Cannot write gameplay result", error); }
        Minecraft.getInstance().stop();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
