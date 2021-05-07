package me.KP56.FakePlayers.BungeeCord.Listeners;

import me.KP56.FakePlayers.BungeeCord.Main;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerConnectListener implements Listener {
    @EventHandler
    public void onServerConnect(ServerConnectEvent e) {
        String playerName = e.getPlayer().getName();

        if (Main.getMain().isFakePlayer(playerName)) {
            if (e.getPlayer().getServer() != null) {
                e.setCancelled(true);

                ServerConnection serverConnection = (ServerConnection) e.getPlayer().getServer();
                ServerConnection target = new ServerConnection(((UserConnection) e.getPlayer()).getCh(), (BungeeServerInfo) e.getTarget());
                UserConnection fakePlayer = (UserConnection) e.getPlayer();

                String ip = e.getPlayer().getServer().getAddress().toString();
                String ip2 = e.getTarget().getAddress().toString();

                int serversPort = Main.getMain().configuration.getInt("servers." + e.getPlayer().getServer().getInfo().getName() + ".fakeplayers-port");
                int targetsPort = Main.getMain().configuration.getInt("servers." + e.getTarget().getName() + ".fakeplayers-port");

                Main.getMain().disband(playerName, ip, serversPort);
                Main.getMain().summon(playerName, ip2, targetsPort);

                fakePlayer.setServer(serverConnection);

                serverConnection.getInfo().removePlayer(fakePlayer);
                target.getInfo().addPlayer(fakePlayer);
            }
        }
    }
}
