package dev.worldmusic;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;

@Mod(value = WorldMusic.MOD_ID, dist = Dist.CLIENT)
public final class WorldMusic {
    public static final String MOD_ID = "world_music";

    public WorldMusic(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, WorldMusicConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        var player = new WorldMusicPlayer(container);
        NeoForge.EVENT_BUS.addListener(player::tick);
        NeoForge.EVENT_BUS.addListener(player::selectMusic);
        NeoForge.EVENT_BUS.addListener(player::registerCommands);
        NeoForge.EVENT_BUS.addListener((ClientResourceLoadFinishedEvent event) -> player.invalidate());
    }
}
