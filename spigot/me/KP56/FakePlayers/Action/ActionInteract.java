package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.List;

public class ActionInteract implements Action {
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

            PlayerInteractEntityEvent e = new PlayerInteractEntityEvent(p, closestEntity);
            Bukkit.getPluginManager().callEvent(e);
        }
    }

    @Override
    public ActionType getType() {
        return ActionType.INTERACT;
    }
}
