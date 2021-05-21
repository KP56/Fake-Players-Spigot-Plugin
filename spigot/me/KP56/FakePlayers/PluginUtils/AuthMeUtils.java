package me.KP56.FakePlayers.PluginUtils;

import fr.xephi.authme.AuthMe;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuthMeUtils {
    public static void unregisterHandlers() {
        AuthMe plugin = JavaPlugin.getPlugin(AuthMe.class);
        HandlerList.unregisterAll(plugin);
    }
}
