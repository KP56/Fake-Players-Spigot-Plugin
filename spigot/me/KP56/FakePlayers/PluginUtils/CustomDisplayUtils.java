package me.KP56.FakePlayers.PluginUtils;

import com.daxton.customdisplay.CustomDisplay;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomDisplayUtils {
    public static void unregisterHandlers() {
        Plugin plugin = JavaPlugin.getPlugin(CustomDisplay.class);
        HandlerList.unregisterAll(plugin);
    }
}
