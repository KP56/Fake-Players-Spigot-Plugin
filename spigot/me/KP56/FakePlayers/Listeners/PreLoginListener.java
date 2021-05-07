package me.KP56.FakePlayers.Listeners;

import me.KP56.FakePlayers.FakePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPreLoginEvent;

public class PreLoginListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void preLoginListener(PlayerPreLoginEvent e) {
        FakePlayer player = FakePlayer.getFakePlayer(e.getName());
        if (player != null) {
            player.removePlayer();
        }
    }
}
