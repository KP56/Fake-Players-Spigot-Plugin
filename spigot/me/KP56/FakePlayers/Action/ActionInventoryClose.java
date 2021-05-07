package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionInventoryClose implements Action {
    @Override
    public void perform(FakePlayer player) {
        Player p = Bukkit.getPlayer(player.getUUID());

        p.closeInventory();
    }

    @Override
    public ActionType getType() {
        return ActionType.INVENTORY_CLOSE;
    }
}
