package me.KP56.FakePlayers;

import de.jeff_media.updatechecker.UpdateChecker;
import me.KP56.FakePlayers.Commands.FakePlayers;
import me.KP56.FakePlayers.Listeners.DeathListener;
import me.KP56.FakePlayers.Listeners.PreLoginListener;
import me.KP56.FakePlayers.MultiVersion.Version;
import me.KP56.FakePlayers.Socket.FakePlayersSocket;
import me.KP56.FakePlayers.TabComplete.FakePlayersTabComplete;
import me.KP56.FakePlayers.Utils.Color;
import me.KP56.FakePlayers.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Main extends JavaPlugin {

    private static final int SPIGOT_RESOURCE_ID = 91163;
    private static Main plugin;
    public FileConfiguration config;

    private boolean usesCraftBukkit = false;
    private boolean usesPaper = false;
    private boolean updatedPaper = false;
    private boolean usesProtocolLib = false;
    private Version version = Version.valueOf(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);

    public static Main getPlugin() {
        return plugin;
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

    public static UUID getRandomUUID(String name) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

        return offlinePlayer.getUniqueId();
    }

    public boolean usesPaper() {
        return usesPaper;
    }

    public boolean isPaperUpdated() {
        return updatedPaper;
    }

    public Version getVersion() {
        return version;
    }

    private void checkForClasses() {
        try {
            usesPaper = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;

            if (usesPaper) {
                Bukkit.getLogger().info("Paper detected.");
            }
        } catch (ClassNotFoundException ignored) {

        }

        try {
            updatedPaper = Class.forName("net.kyori.adventure.text.ComponentLike") != null;
        } catch (ClassNotFoundException ignored) {

        }

        try {
            usesCraftBukkit = Class.forName("org.spigotmc.SpigotConfig") == null;
        } catch (ClassNotFoundException ignored) {
            usesCraftBukkit = true;
        }

        try {
            this.usesProtocolLib = (Class.forName("com.comphenix.protocol.ProtocolLib") != null);
        } catch (ClassNotFoundException ignored) {

        }
    }

    @Override
    public void onEnable() {

        File macrosFolder = new File("plugins/FakePlayers/macros");
        if (!macrosFolder.exists()) {
            macrosFolder.mkdir();
        }

        File cacheFolder = new File("plugins/FakePlayers/cache");
        if (!cacheFolder.exists()) {
            cacheFolder.mkdir();
        }

        if (version == null) {
            Bukkit.getLogger().warning("This spigot version is not supported by Fake Players!");
            Bukkit.getLogger().warning("This spigot version is not supported by Fake Players!");
            Bukkit.getLogger().warning("This spigot version is not supported by Fake Players!");
        }

        Bukkit.getLogger().info("Detected version: " + version.name());

        getCommand("fakeplayers").setExecutor(new FakePlayers());
        getCommand("fakeplayers").setTabCompleter(new FakePlayersTabComplete());

        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new PreLoginListener(), this);

        plugin = this;

        checkForClasses();

        this.saveDefaultConfig();
        config = this.getConfig();

        validateConfig();

        if (config.getBoolean("update-notifications") && !usesCraftBukkit) {
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

        if (!config.getBoolean("bungeecord.enabled")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                File cache = new File("plugins/FakePlayers/cache/cache$1.fpcache");
                if (cache.exists()) {
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader("plugins/FakePlayers/cache/cache$1.fpcache"));

                        String line = reader.readLine();

                        while (line != null) {
                            FakePlayer.summon(line);

                            line = reader.readLine();
                        }

                        reader.close();
                    } catch (IOException e) {
                        Bukkit.getLogger().warning("Failed to read from cache. Fake players from last server instance won't rejoin.");
                    }

                    cache.delete();
                }
            }, 100);
        }
        if (config.getBoolean("bungeecord.enabled")) {
            System.out.println("Starting socket...");
            FakePlayersSocket.fakePlayersSocket.start(config.getString("bungeecord.ip"), config.getInt("bungeecord.fakeplayers-port"));
        }
    }

    public boolean usesCraftBukkit() {
        return usesCraftBukkit;
    }

    @Override
    public void onDisable() {
        List<FakePlayer> copyList = new ArrayList<>(FakePlayer.getFakePlayers());
        try {
            BufferedWriter myWriter = new BufferedWriter(new FileWriter("plugins/FakePlayers/cache/cache$1.fpcache"));

            for (FakePlayer player : copyList) {
                myWriter.write(player.getName());

                player.removePlayer();
            }

            myWriter.close();
        } catch (IOException e) {
            Bukkit.getLogger().warning("Failed to cache fake players who are currently online. They will not rejoin your server.");
        }
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

    public boolean usesProtocolLib() {
        return usesProtocolLib;
    }
}
