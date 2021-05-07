package me.KP56.FakePlayers.Listeners;

import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (FakePlayer.getFakePlayer(e.getEntity().getUniqueId()) != null) {
            if (!Main.getPlugin().usesCraftBukkit()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> e.getEntity().spigot().respawn(), 20);
            } else {
                Bukkit.getLogger().warning("Auto respawn feature is not supported, when using CraftBukkit.");
            }
        }
    }
}
