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
        FakePlayer fakePlayer = FakePlayer.getFakePlayer(e.getEntity().getUniqueId());
        if (fakePlayer != null) {
            if (Main.getPlugin().usesPaper()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> e.getEntity().spigot().respawn(), 20);
            }
        }
    }
}
