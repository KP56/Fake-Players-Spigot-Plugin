package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import org.bukkit.Bukkit;

public class ActionChat implements Action {

    private String message;

    public ActionChat(String message) {
        this.message = message;
    }

    @Override
    public void perform(FakePlayer player) {
       Bukkit.getPlayer(player.getName()).chat(message);
    }

    @Override
    public ActionType getType() {
        return ActionType.CHAT;
    }

    public String getMessage() {
        return message;
    }
}
