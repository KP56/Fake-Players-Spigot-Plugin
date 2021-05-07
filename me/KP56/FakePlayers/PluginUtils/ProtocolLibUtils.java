package me.KP56.FakePlayers.PluginUtils;

import com.comphenix.protocol.ProtocolLib;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProtocolLibUtils {
    public static void unregisterHandlers() {
        ProtocolLib plugin = JavaPlugin.getPlugin(ProtocolLib.class);
        HandlerList.unregisterAll(plugin);
    }
}