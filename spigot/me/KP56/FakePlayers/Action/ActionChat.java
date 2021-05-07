package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Main;
import me.KP56.FakePlayers.Socket.FakePlayersSocket;
import org.bukkit.Bukkit;

import java.io.IOException;

public class ActionChat implements Action {

    private String message;

    public ActionChat(String message) {
        this.message = message;
    }

    @Override
    public void perform(FakePlayer player) {
        FakePlayersSocket.fakePlayersSocket.send(Main.getPlugin().config.getString("bungeecord.ip"), Main.getPlugin().config.getInt("bungeecord.bungeecord-fakeplayers-port"), "chat " + player.getName() + " " + message);
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
