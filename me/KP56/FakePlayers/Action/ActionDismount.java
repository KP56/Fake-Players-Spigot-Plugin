package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ActionDismount implements Action {
    @Override
    public void perform(FakePlayer player) {
        Player p = Bukkit.getPlayer(player.getUUID());

        Entity mount = p.getVehicle();

        if (mount != null) {
            mount.eject();
        }
    }

    @Override
    public ActionType getType() {
        return ActionType.DISMOUNT;
    }
}
