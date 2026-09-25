package dev.worldmusic;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class WorldMusicConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue OVERWORLD_ONLY;
    public static final ModConfigSpec.BooleanValue EMPTY_USES_VANILLA;
    public static final Map<TimePeriod, ModConfigSpec.ConfigValue<List<? extends String>>> TRACKS = new EnumMap<>(TimePeriod.class);

    static {
        var b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Enable scheduled world music. Changes apply in game.", "启用分时段世界音乐，修改后即时生效。")
                .translation("world_music.config.enabled").define("enabled", true);
        OVERWORLD_ONLY = b.comment("Only replace Overworld music. Disable to use the Overworld clock in all dimensions.", "仅替换主世界音乐；关闭后所有维度按主世界时钟播放。")
                .translation("world_music.config.overworldOnly").define("overworldOnly", true);
        EMPTY_USES_VANILLA = b.comment("Use vanilla music when a playlist is empty or all its sounds are missing. False means silence.", "空歌单或所有曲目无效时恢复原版音乐；关闭则保持安静。")
                .translation("world_music.config.emptyUsesVanilla").define("emptyUsesVanilla", true);
        String[][] defaults = {
            {"minecraft", "clark", "sweden"},
            {"subwoofer_lullaby", "living_mice", "haggstrom"},
            {"danny", "key", "oxygene"},
            {"dry_hands", "wet_hands", "mice_on_venus"}
        };
        for (TimePeriod period : TimePeriod.values()) {
            var initial = java.util.Arrays.stream(defaults[period.ordinal()]).map(s -> "world_music:vanilla." + s).toList();
            TRACKS.put(period, b.comment("0-10 sound event IDs; duplicates are ignored. Custom slot example: world_music:" + period.key() + "_1",
                            "最多10首声音事件ID，可使用原版、其他模组或资源包音乐；空列表=[]。")
                    .translation("world_music.config." + period.key())
                    .defineList(List.of(period.key()), () -> initial,
                            () -> "world_music:" + period.key() + "_1",
                            value -> value instanceof String s && Identifier.tryParse(s) != null,
                            ModConfigSpec.Range.of(0, Playlist.MAX_TRACKS)));
        }
        SPEC = b.build();
    }

    private WorldMusicConfig() {}
}
