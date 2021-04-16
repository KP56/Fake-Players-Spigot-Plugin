package me.KP56.FakePlayers;

import de.jeff_media.updatechecker.UpdateChecker;
import me.KP56.FakePlayers.Commands.FakePlayers;
import me.KP56.FakePlayers.MultiVersion.Version;
import me.KP56.FakePlayers.Utils.Color;
import me.KP56.FakePlayers.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

public class Main extends JavaPlugin {

    private static final int SPIGOT_RESOURCE_ID = 91163;
    public static FileConfiguration config;
    private static Plugin plugin;
    private static boolean usesPaper = false;
    private static boolean updatedPaper = false;
    private static Version version = Version.valueOf(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);

    @Override
    public void onEnable() {

        if (version == null) {
            Bukkit.getLogger().warning("This spigot version is not supported by Fake Players!");
            Bukkit.getLogger().warning("This spigot version is not supported by Fake Players!");
            Bukkit.getLogger().warning("This spigot version is not supported by Fake Players!");
        }

        Bukkit.getLogger().info("Detected version: " + version.name());

        getCommand("fakeplayers").setExecutor(new FakePlayers());

        plugin = this;

        try {
            usesPaper = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
            updatedPaper = Class.forName("net.kyori.adventure.text.ComponentLike") != null;
            if (usesPaper) {
                Bukkit.getLogger().info("Paper detected.");
            }
        } catch (ClassNotFoundException ignored) {

        }

        this.saveDefaultConfig();
        config = this.getConfig();

        validateConfig();

        if (config.getBoolean("update-notifications")) {
            UpdateChecker.init(this, SPIGOT_RESOURCE_ID)
                    .setDownloadLink(SPIGOT_RESOURCE_ID)
                    .setNotifyByPermissionOnJoin("fakeplayers.notify")
                    .setNotifyOpsOnJoin(true)
                    .checkEveryXHours(6)
                    .checkNow();
        }


        if (config.getBoolean("bstats")) {
            Metrics metrics = new Metrics(this, 11025);
        }
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static boolean usesPaper() {
        return usesPaper;
    }

    public static boolean isPaperUpdated() {
        return updatedPaper;
    }

    public static String getConfigMessage(FileConfiguration config, String path, String[] args) {
        String text = config.getString(path);

        boolean open = false;
        StringBuilder chars = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '%') {
                if (open) {
                    final char[] CHARACTERS = chars.toString().toCharArray();
                    if (CHARACTERS[0] == 'a' && CHARACTERS[1] == 'r' && CHARACTERS[2] == 'g') {
                        final int ARG = Integer.parseInt(String.valueOf(CHARACTERS[3]));

                        text = text.replace(chars.toString(), args[ARG]);

                        chars = new StringBuilder();
                    }
                    open = false;
                } else {
                    open = true;
                }
                continue;
            }

            if (open) {
                chars.append(c);
            }
        }

        return Color.format(config.getString("prefix") + " " + text.replace("%", ""));
    }

    public static Version getVersion() {
        return version;
    }

    private void validateConfig() {
        InputStream is = getResource("config.yml");

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(new InputStreamReader(is));

        Set<String> pluginKeys = configuration.getKeys(true);
        Set<String> configKeys = config.getKeys(true);

        for (String s : pluginKeys) {
            if (!configKeys.contains(s)) {
                System.out.println("You are using an invalid version of Fake Players config. Creating a new one...");
                new File("plugins/FakePlayers/config.yml").delete();
                this.saveDefaultConfig();

                return;
            }
        }
    }
}
