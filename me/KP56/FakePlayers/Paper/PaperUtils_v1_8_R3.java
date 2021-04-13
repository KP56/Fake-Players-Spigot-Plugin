package me.KP56.FakePlayers.Paper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PaperUtils_v1_8_R3 {
    public static void playerInitialSpawnEvent(Player p) {
        PlayerSpawnLocationEvent ev = new PlayerSpawnLocationEvent(p, p.getLocation());

        Bukkit.getPluginManager().callEvent(ev);
    }
}
