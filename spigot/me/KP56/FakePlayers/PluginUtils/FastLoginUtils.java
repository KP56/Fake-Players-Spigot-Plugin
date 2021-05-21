package me.KP56.FakePlayers.PluginUtils;

import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class FastLoginUtils {
    public static void unregisterHandlers() {
        FastLoginBukkit plugin = JavaPlugin.getPlugin(FastLoginBukkit.class);
        HandlerList.unregisterAll(plugin);
    }
}
