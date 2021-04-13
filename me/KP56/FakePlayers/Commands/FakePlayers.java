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
                sender.sendMessage(Color.format("&2> &3/FakePlayers reload &b- reloads config.yml"));
            } else {
                switch (args[0]) {
                    case "summon":
                        if (args.length == 2) {
                            summon(sender, args[1], args);
                        } else if (args.length == 3) {
                            summon(sender, args[1], Integer.parseInt(args[2]), args);
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

                            chat(sender, args[1], message.toString(), args);
                        } else {
                            Bukkit.dispatchCommand(sender, "fakeplayers");
                        }
                        break;
                    case "disband":
                        if (args.length == 2) {
                            disband(sender, args[1], args);
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
                    case "reload":
                        Bukkit.getPluginManager().disablePlugin(Main.getPlugin());
                        Bukkit.getPluginManager().getPlugin("FakePlayers").reloadConfig();
                        Bukkit.getPluginManager().enablePlugin(Main.getPlugin());
                        sender.sendMessage(Main.getConfigMessage(Main.config, "messages.reload", args));
                        break;
                }
            }
        } else {
            sender.sendMessage(Main.getConfigMessage(Main.config, "messages.no-permissions", args));
        }
        return true;
    }

    private void summon(CommandSender sender, String name, int number, String[] args) {
        if (number == 1) {
            if (new FakePlayer(UUID.randomUUID(), name, Bukkit.getServer().getWorlds().get(0).getSpawnLocation()).spawn()) {
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.summon.success-one", args));
            } else {
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.summon.failed", args));
            }
        } else if (number < 1) {
            sender.sendMessage(Main.getConfigMessage(Main.config, "messages.summon.incorrect-number", args));
        } else {
            int addition = 0;
            for (int i = 1; FakePlayer.getFakePlayer(name + i) != null; i++) {
                addition = i;
            }

            for (int i = addition; i < number + addition; i++) {
                int finalI = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    new FakePlayer(UUID.randomUUID(), name + (finalI + 1), Bukkit.getServer().getWorlds().get(0).getSpawnLocation()).spawn();
                }, finalI * Main.config.getInt("tick-delay-between-joins"));
            }
            sender.sendMessage(Main.getConfigMessage(Main.config, "messages.summon.trying-amount", args));
        }
    }

    private void summon(CommandSender sender, String name, String[] args) {
        summon(sender, name, 1, args);
    }

    private void chat(CommandSender sender, String name, String message, String[] args) {
        if (name.equalsIgnoreCase("All")) {
            for (FakePlayer player : FakePlayer.getFakePlayers()) {
                Bukkit.getPlayer(player.getName()).chat(message);
            }
        } else {
            final Player PLAYER = Bukkit.getPlayer(name);
            if (PLAYER != null) {
                if (FakePlayer.getFakePlayer(name) != null) {
                    PLAYER.chat(message);
                } else {
                    sender.sendMessage(Main.getConfigMessage(Main.config, "messages.chat.not-a-fake-player", args));
                }
            } else {
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.chat.player-is-not-online", args));
            }
        }
    }

    private void disband(CommandSender sender, String name, String[] args) {

        if (name.equalsIgnoreCase("All")) {
            final List<FakePlayer> copy = new ArrayList<>(FakePlayer.getFakePlayers());
            for (FakePlayer player : copy) {
                player.removePlayer();
            }
            sender.sendMessage(Main.getConfigMessage(Main.config, "messages.disband.all-disbanded-success", args));
        } else {
            final FakePlayer FAKE_PLAYER = FakePlayer.getFakePlayer(name);

            if (FAKE_PLAYER != null) {
                FAKE_PLAYER.removePlayer();
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.disband.one-disbanded-success", args));
            } else {
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.disband.failed", args));
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

            String[] lists = new String[(int) Math.ceil((double) list.length() / 48.0d)];

            int cuts = 0;
            int lastCut = 0;
            StringBuilder listToAdd = new StringBuilder();
            for (int i = 0; i < list.length(); i++) {
                char c = list.charAt(i);

                if (c == ',' && i - lastCut >= 48) {
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
