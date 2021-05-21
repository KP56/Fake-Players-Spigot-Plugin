package me.KP56.FakePlayers.PluginUtils;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import su.nexmedia.engine.NexPlugin;

public final class NexEngineUtils {
    public static void unregisterHandlers() {
        NexPlugin plugin = JavaPlugin.getPlugin(NexPlugin.class);
        HandlerList.unregisterAll(plugin);
    }
}
