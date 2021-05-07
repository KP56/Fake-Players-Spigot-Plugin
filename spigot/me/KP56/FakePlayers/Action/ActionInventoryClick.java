package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class ActionInventoryClick implements Action {

    private int slot;

    public ActionInventoryClick(int slot) {
        this.slot = slot;
    }

    @Override
    public void perform(FakePlayer player) {
        Player p = Bukkit.getPlayer(player.getUUID());

        InventoryView view = p.getOpenInventory();

        InventoryClickEvent e = new InventoryClickEvent(view, InventoryType.SlotType.CONTAINER, slot, ClickType.LEFT, InventoryAction.SWAP_WITH_CURSOR);
        Bukkit.getPluginManager().callEvent(e);

        if (!e.isCancelled()) {
            ItemStack onCursor = p.getItemOnCursor().clone();
            p.setItemOnCursor(view.getItem(slot));
            view.setItem(slot, onCursor);
        }
    }

    @Override
    public ActionType getType() {
        return ActionType.INVENTORY_CLICK;
    }

    public int getSlot() {
        return slot;
    }
}
