package dev.worldmusic.smoke;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import dev.worldmusic.TimePeriod;
import dev.worldmusic.WorldMusicConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod(value = "world_music_smoke", dist = Dist.CLIENT)
public final class SmokeChecks {
    private boolean finished;

    public SmokeChecks() {
        NeoForge.EVENT_BUS.addListener(this::loaded);
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            if (finished) Minecraft.getInstance().stop();
        });
    }

    private void loaded(ClientResourceLoadFinishedEvent event) {
        if (!event.isInitial()) return;
        try {
            check(WorldMusicConfig.SPEC.isLoaded(), "Client config loaded");
            int tracks = 0;
            for (TimePeriod period : TimePeriod.values()) {
                for (String id : WorldMusicConfig.TRACKS.get(period).get()) {
                    var sound = Minecraft.getInstance().getSoundManager().getSoundEvent(Identifier.parse(id));
                    check(sound != null && sound.getWeight() > 0, "Default track resolves: " + id);
                    tracks++;
                }
            }
            check(tracks == 12, "All 12 bundled vanilla aliases resolve");
            if (Boolean.getBoolean("world_music.packChecks")) {
                for (TimePeriod period : TimePeriod.values()) {
                    for (int index = 1; index <= 10; index++) {
                        var id = Identifier.parse("world_music:" + period.key() + "_" + index);
                        var sound = Minecraft.getInstance().getSoundManager().getSoundEvent(id);
                        check(sound != null && sound.getWeight() > 0, "Custom music resolves: " + id);
                    }
                }
                LogUtils.getLogger().info("WORLD_MUSIC_PACK_PASS: all 40 custom music events loaded");
            }
            CommentedConfig config = CommentedConfig.inMemory();
            WorldMusicConfig.SPEC.correct(config, (action, path, oldValue, newValue) -> {});
            check(WorldMusicConfig.SPEC.isCorrect(config), "Default config valid");
            config.set("morning", List.of());
            check(WorldMusicConfig.SPEC.isCorrect(config), "Empty playlist accepted");
            config.set("morning", IntStream.rangeClosed(1, 10).mapToObj(i -> "world_music:morning_" + i).toList());
            check(WorldMusicConfig.SPEC.isCorrect(config), "Ten-track playlist accepted");
            ModConfigSpec.ListValueSpec listSpec = WorldMusicConfig.SPEC.getSpec().get("morning");
            check(listSpec.getSizeRange().test(10), "UI allows ten tracks");
            check(!listSpec.getSizeRange().test(11), "UI rejects eleven tracks");
            config.set("morning", List.of("Invalid ID!"));
            check(!WorldMusicConfig.SPEC.isCorrect(config), "Malformed sound ID rejected");
            Files.writeString(Path.of("smoke-result.txt"), "PASS: Minecraft 26.2 / NeoForge 26.2.0.67 loaded World Music; all 12 default sounds resolve; empty and ten-track configs accepted; UI caps lists at ten; malformed IDs rejected.\n");
            LogUtils.getLogger().info("WORLD_MUSIC_SMOKE_PASS");
            if (Boolean.getBoolean("world_music.gameplayChecks")) {
                new GameplayChecks();
                return;
            }
        } catch (Throwable error) {
            LogUtils.getLogger().error("WORLD_MUSIC_SMOKE_FAIL", error);
            try {
                Files.writeString(Path.of("smoke-result.txt"), "FAIL: " + error);
            } catch (Exception ignored) {}
        }
        finished = true;
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError(description);
    }
}
