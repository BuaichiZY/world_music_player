package dev.worldmusic;

import com.mojang.logging.LogUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.slf4j.Logger;

/** Owns only scheduled background music; never stops records or other sound categories. */
public final class WorldMusicPlayer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ModContainer container;
    private final Random random = new Random();
    private final PeriodPlayback playback = new PeriodPlayback();
    private final Set<Identifier> failed = new HashSet<>();
    private ClientLevel level;
    private TimePeriod period;
    private List<? extends String> configured = List.of();
    private List<Identifier> available = List.of();
    private SoundInstance current;
    private Identifier previous;
    private int startupGrace;
    private boolean wasActive;
    private boolean controlling;
    private boolean dirty = true;
    private boolean openConfig;

    public WorldMusicPlayer(ModContainer container) {
        this.container = container;
    }

    public void invalidate() {
        dirty = true;
    }

    public void tick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            dirty |= playback.update(mc.level.getOverworldClockTime());
        } else if (mc.getConnection() == null) {
            playback.reset();
        }
        if (openConfig) {
            openConfig = false;
            mc.setScreen(new ConfigurationScreen(container, mc.screen));
        }
        if (!eligible(mc)) {
            stop(mc);
            controlling = false;
            level = null;
            period = null;
            previous = null;
            dirty = true;
            return;
        }

        TimePeriod nextPeriod = TimePeriod.at(mc.level.getOverworldClockTime());
        List<? extends String> nextConfig = WorldMusicConfig.TRACKS.get(nextPeriod).get();
        if (dirty || level != mc.level || period != nextPeriod || !configured.equals(nextConfig)) {
            stop(mc);
            if (level != mc.level) previous = null;
            level = mc.level;
            period = nextPeriod;
            configured = List.copyOf(nextConfig);
            if (configured.size() > Playlist.MAX_TRACKS) {
                LOGGER.warn("World Music: {} playlist has more than ten entries; only the first ten are used", period);
            }
            failed.clear();
            available = configured.stream().limit(Playlist.MAX_TRACKS).map(Identifier::tryParse)
                    .filter(java.util.Objects::nonNull).distinct().filter(id -> {
                        var sound = mc.getSoundManager().getSoundEvent(id);
                        if (sound != null && sound.getWeight() > 0) return true;
                        LOGGER.warn("World Music: missing or empty sound event {} in {} playlist; skipping", id, period);
                        return false;
                    }).toList();
            dirty = false;
        }

        List<Identifier> playable = available.stream().filter(id -> !failed.contains(id)).toList();
        // Keep vanilla suppressed after our song ends, including after a config/resource reload.
        boolean shouldControl = playback.used() || !playable.isEmpty() || !WorldMusicConfig.EMPTY_USES_VANILLA.get();
        if (shouldControl && !controlling) mc.getMusicManager().stopPlaying();
        controlling = shouldControl;
        if (!controlling) {
            stop(mc);
            return;
        }
        if (mc.isPaused() || mc.getOverlay() != null) return;
        if (mc.options.getSoundSourceVolume(SoundSource.MUSIC) <= 0
                || mc.options.getSoundSourceVolume(SoundSource.MASTER) <= 0) {
            stop(mc);
            return;
        }
        if (current != null) {
            if (mc.getSoundManager().isActive(current)) {
                wasActive = true;
                startupGrace = 0;
                return;
            }
            // Audio streams open asynchronously. Do not queue a new copy while a stream is starting.
            if (startupGrace-- > 0) return;
            if (!wasActive) {
                failed.add(current.getIdentifier());
                LOGGER.warn("World Music: {} could not start; skipping until playlist/resource reload", current.getIdentifier());
            }
            stop(mc);
        }
        if (playback.used()) return;
        playable = available.stream().filter(id -> !failed.contains(id)).toList();
        Identifier chosen = Playlist.choose(playable, previous, random);
        if (chosen == null) return;
        // Clear vanilla's last environmental fade before starting the replacement track.
        mc.getSoundManager().updateCategoryVolume(SoundSource.MUSIC, 1.0F);
        current = SimpleSoundInstance.forMusic(SoundEvent.createVariableRangeEvent(chosen));
        SoundEngine.PlayResult result = mc.getSoundManager().play(current);
        if (result == SoundEngine.PlayResult.NOT_STARTED) {
            failed.add(chosen);
            LOGGER.warn("World Music: failed to play {}; skipping until playlist/resource reload", chosen);
            stop(mc);
            return;
        }
        previous = chosen;
        playback.consume();
        startupGrace = 40;
        wasActive = false;
        LOGGER.debug("World Music: playing {} in {}", chosen, period);
    }

    public void selectMusic(SelectMusicEvent event) {
        if (controlling && eligible(Minecraft.getInstance())) event.overrideMusic(null);
    }

    private boolean eligible(Minecraft mc) {
        return mc.level != null && mc.player != null && WorldMusicConfig.SPEC.isLoaded()
                && WorldMusicConfig.ENABLED.get()
                && (!WorldMusicConfig.OVERWORLD_ONLY.get() || mc.level.dimension().equals(Level.OVERWORLD));
    }

    private void stop(Minecraft mc) {
        if (current != null) mc.getSoundManager().stop(current);
        current = null;
        startupGrace = 0;
        wasActive = false;
    }

    public void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("worldmusic")
                .executes(context -> status())
                .then(Commands.literal("status").executes(context -> status()))
                .then(Commands.literal("config").executes(context -> {
                    openConfig = true;
                    return 1;
                }))
                .then(Commands.literal("next").executes(context -> {
                    stop(Minecraft.getInstance());
                    playback.consume();
                    message(Component.translatable("world_music.message.skipped"));
                    return 1;
                }))
                .then(Commands.literal("reload").executes(context -> {
                    invalidate();
                    message(Component.translatable("world_music.message.reloaded"));
                    return 1;
                })));
    }

    private int status() {
        message(Component.translatable("world_music.message.status",
                period == null ? Component.translatable("world_music.state.inactive")
                        : Component.translatable("world_music.config." + period.key()),
                current == null ? Component.translatable(controlling
                        ? (playback.used() ? "world_music.state.finished" : "world_music.state.waiting") : "world_music.state.vanilla")
                        : Component.literal(current.getIdentifier().toString()),
                available.stream().filter(id -> !failed.contains(id)).count()));
        return 1;
    }

    private void message(Component text) {
        var player = Minecraft.getInstance().player;
        if (player != null) player.sendSystemMessage(text);
    }
}
