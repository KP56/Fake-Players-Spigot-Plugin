package me.KP56.FakePlayers.Spigot;

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public final class SpigotUtils {
    public static void setResourcePackStatus(CraftPlayer bukkitPlayer, PlayerResourcePackStatusEvent.Status status) {
        bukkitPlayer.setResourcePackStatus(status);
    }
}
