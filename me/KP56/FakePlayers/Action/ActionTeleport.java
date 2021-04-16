package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ActionTeleport implements Action {

    private Location teleportLocation;
    private Player player;

    public ActionTeleport(Location teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public ActionTeleport(Player player) {
        this.player = player;
    }

    @Override
    public void perform(FakePlayer player) {
        Player p = Bukkit.getPlayer(player.getUUID());
        if (teleportLocation != null) {
            p.teleport(teleportLocation);
        } else {
            p.teleport(this.player.getLocation());
        }
    }
}
