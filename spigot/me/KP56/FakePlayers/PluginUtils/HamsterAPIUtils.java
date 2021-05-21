package me.KP56.FakePlayers.PluginUtils;

import dev._2lstudios.hamsterapi.HamsterAPI;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class HamsterAPIUtils {
    public static void unregisterHandlers() {
        HamsterAPI plugin = JavaPlugin.getPlugin(HamsterAPI.class);
        HandlerList.unregisterAll(plugin);
    }
}
