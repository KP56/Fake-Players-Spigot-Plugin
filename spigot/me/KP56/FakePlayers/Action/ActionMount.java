package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class ActionMount implements Action {
    @Override
    public void perform(FakePlayer player) {
        Player p = Bukkit.getPlayer(player.getUUID());

        List<Entity> near = p.getNearbyEntities(3, 3, 3);

        Entity closestEntity = null;
        double lowestDistance = Double.MAX_VALUE;

        for (Entity entity : near) {
            double distance = entity.getLocation().distance(p.getLocation());
            if (distance < lowestDistance) {
                lowestDistance = distance;
                closestEntity = entity;
            }
        }

        if (closestEntity != null) {
            p.teleport(p.getLocation().setDirection(p.getLocation().subtract(closestEntity.getLocation()).toVector()));

            closestEntity.setPassenger(p);
        }
    }

    @Override
    public ActionType getType() {
        return ActionType.MOUNT;
    }
}
