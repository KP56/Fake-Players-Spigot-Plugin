package me.KP56.FakePlayers;

import me.KP56.FakePlayers.Commands.FakePlayers;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Plugin plugin;

    private static boolean usesPaper;

    @Override
    public void onEnable() {
        getCommand("fakeplayers").setExecutor(new FakePlayers());

        plugin = this;

        try {
            usesPaper = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
        } catch (ClassNotFoundException ignored) {

        }
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static boolean usesPaper() {
        return usesPaper;
    }
}
