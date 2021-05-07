package me.KP56.FakePlayers.BungeeCord.Listeners;

import me.KP56.FakePlayers.BungeeCord.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class CommandListener implements Listener {
    @EventHandler
    public void onCommand(ChatEvent e) {
        if (e.getMessage().charAt(0) == '/') {
            String label = e.getMessage().substring(1);

            String[] labelArr = label.split(" ");
            String command = labelArr[0];

            if (command.equalsIgnoreCase("fakeplayers")) {
                String[] args = new String[labelArr.length - 1];

                if (args.length >= 2) {
                    System.arraycopy(labelArr, 1, args, 0, labelArr.length - 1);

                    ServerConnection serverConnection = (ServerConnection) e.getReceiver();

                    new Thread(() -> {
                        if (args[0].equalsIgnoreCase("summon")) {
                            if (args.length == 2) {
                                if (BungeeCord.getInstance().getPlayer(args[1]) == null && args[1].length() <= 16) {
                                    Main.getMain().addFakePlayer(args[1], serverConnection);
                                } else {
                                    e.setCancelled(true);
                                }
                            } else {
                                int addition = 0;
                                for (int i = 1; Main.getMain().getFakePlayer(args[1] + i) != null; i++) {
                                    addition = i;
                                }

                                for (int i = addition; i < Integer.parseInt(args[2]) + addition; i++) {
                                    if (BungeeCord.getInstance().getPlayer(args[1] + (i + 1)) == null && (args[1] + (i + 1)).length() <= 16) {
                                        Main.getMain().addFakePlayer(args[1] + (i + 1), serverConnection);
                                    } else {
                                        e.setCancelled(true);
                                    }
                                }
                            }
                        } else if (args[0].equalsIgnoreCase("disband")) {
                            if (args.length == 2) {
                                if (args[1].equalsIgnoreCase("all")) {
                                    Main.getMain().removeAll(serverConnection);
                                } else {
                                    Main.getMain().removeFakePlayer(args[1]);
                                }
                            }
                        }
                    }).start();
                }
            }
        }
    }
}
