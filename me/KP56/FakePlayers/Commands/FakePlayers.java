package me.KP56.FakePlayers.Commands;

import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Main;
import me.KP56.FakePlayers.Utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayers implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.hasPermission("fakeplayers.cmd")) {
            if (args.length == 0) {
                sender.sendMessage(Color.format("&2>> &aFake players - &bHelp (Page 1/1)"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers summon (Name) &b- summons a fake player"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers summon (Name) (Number) &b- summons a certain amount of fake players"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers disband (Name/All) &b- disbands fake players"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers chat (Name/All) (Chat Message) &b- makes fake players type something in chat (it can be a command)"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers list &b- displays a fake player list"));
            } else {
                switch (args[0]) {
                    case "summon":
                        if (args.length == 2) {
                            summon(sender, args[1]);
                        } else if (args.length == 3) {
                            summon(sender, args[1], Integer.parseInt(args[2]));
                        } else {
                            Bukkit.dispatchCommand(sender, "fakeplayers");
                        }
                        break;
                    case "chat":
                        if (args.length >= 3) {
                            StringBuilder message = new StringBuilder();

                            for (int i = 2; i < args.length; i++) {
                                message.append(args[i]).append(" ");
                            }

                            chat(sender, args[1], message.toString());
                        } else {
                            Bukkit.dispatchCommand(sender, "fakeplayers");
                        }
                        break;
                    case "disband":
                        if (args.length == 2) {
                            disband(sender, args[1]);
                        } else {
                            Bukkit.dispatchCommand(sender, "fakeplayers");
                        }
                        break;
                    case "list":
                        if (args.length == 1) {
                            list(sender);
                        } else {
                            Bukkit.dispatchCommand(sender, "fakeplayers");
                        }
                        break;
                }
            }
        } else {
            sender.sendMessage(Color.format("&cYou do not have permissions to execute that command."));
        }
        return true;
    }

    private void summon(CommandSender sender, String name, int number) {
        if (number == 1) {
            if (new FakePlayer(UUID.randomUUID(), name, Bukkit.getServer().getWorlds().get(0).getSpawnLocation()).spawn()) {
                sender.sendMessage(Color.format("&aSuccessfully summoned a Fake Player."));
            } else {
                sender.sendMessage(Color.format("&cFailed to summon a Fake Player."));
            }
        } else if (number < 1) {
            sender.sendMessage(Color.format("&cCannot summon this amount of players."));
        } else {
            int addition = 0;
            for (int i = 1; FakePlayer.getFakePlayer(name + i) != null; i++) {
                addition = i;
            }

            for (int i = addition; i < number + addition; i++) {
                int finalI = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> new FakePlayer(UUID.randomUUID(), name + (finalI + 1), Bukkit.getServer().getWorlds().get(0).getSpawnLocation()).spawn(), finalI * 4);
            }
            sender.sendMessage(Color.format("&aTrying to summon " + number + " Fake Players."));
        }
    }

    private void summon(CommandSender sender, String name) {
        summon(sender, name, 1);
    }

    private void chat(CommandSender sender, String name, String message) {
        if (name.equalsIgnoreCase("All")) {
            for (FakePlayer player : FakePlayer.getFakePlayers()) {
                player.getEntityPlayer().getBukkitEntity().chat(message);
            }
        } else {
            final Player PLAYER = Bukkit.getPlayer(name);
            if (PLAYER != null) {
                if (FakePlayer.getFakePlayer(name) != null) {
                    PLAYER.chat(message);
                } else {
                    sender.sendMessage(Color.format("&cThis is not a Fake Player!"));
                }
            } else {
                sender.sendMessage(Color.format("&cThis player does not exist!"));
            }
        }
    }

    private void disband(CommandSender sender, String name) {

        if (name.equalsIgnoreCase("All")) {
            final List<FakePlayer> copy = new ArrayList<>(FakePlayer.getFakePlayers());
            for (FakePlayer player : copy) {
                player.removePlayer();
            }
            sender.sendMessage(Color.format("&aSuccessfully disbanded all Fake Players."));
        } else {
            final FakePlayer FAKE_PLAYER = FakePlayer.getFakePlayer(name);

            if (FAKE_PLAYER != null) {
                FAKE_PLAYER.removePlayer();
                sender.sendMessage(Color.format("&aSuccessfully disbanded " + name));
            } else {
                sender.sendMessage(Color.format("&cThis player does not exist or is not a Fake Player!"));
            }
        }
    }

    private void list(CommandSender sender) {
        sender.sendMessage(Color.format("&aFake Players (" + FakePlayer.getAmount() + "):"));

        StringBuilder list = new StringBuilder();

        for (FakePlayer player : FakePlayer.getFakePlayers()) {
            list.append(Color.format(player.getName() + ", "));
        }

        if (list.length() >= 3) {
            list = new StringBuilder(list.substring(0, list.length() - 2));

            String[] lists = new String[(int) Math.ceil((double) list.length() / 32.0d)];

            int cuts = 0;
            int lastCut = 0;
            StringBuilder listToAdd = new StringBuilder();
            for (int i = 0; i < list.length(); i++) {
                char c = list.charAt(i);

                if (c == ',' && i - lastCut >= 32) {
                    lastCut = i;
                    lists[cuts] = listToAdd.toString();
                    cuts++;
                    listToAdd = new StringBuilder();
                    i++;
                    continue;
                }
                listToAdd.append(c);
            }

            if (!listToAdd.toString().equals("")) {
                lists[cuts] = listToAdd.toString();
            }

            for (String s : lists) {
                sender.sendMessage(s);
            }
        }
    }
}
