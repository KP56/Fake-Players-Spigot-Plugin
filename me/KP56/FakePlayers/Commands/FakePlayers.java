package me.KP56.FakePlayers.Commands;

import me.KP56.FakePlayers.Action.*;
import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Main;
import me.KP56.FakePlayers.Utils.Color;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
                sender.sendMessage("");
                sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Page 1/1)&2]"));
                sender.sendMessage("");
                sender.sendMessage(Color.format("&2> &3/FakePlayers summon (Name) &b- summons a fake player"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers summon (Name) (Number) &b- summons a certain amount of fake players"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers disband (Name/All) &b- disbands fake players"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers chat (Name/All) (Message/Command) &b- makes fake players type a chat message"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) (Action) &b- makes fake players add a certain action to their list"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) perform &b- makes fake players perform all actions from their list"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) perform (Number) &b- makes fake players perform all actions from their list a certain amount of times"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers action help &b- displays a help page for an action subcommand."));
                sender.sendMessage(Color.format("&2> &3/FakePlayers list &b- displays a fake player list"));
                sender.sendMessage(Color.format("&2> &3/FakePlayers reload &b- reloads config.yml"));
                sender.sendMessage("");
                sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Page 1/1)&2]"));
                sender.sendMessage("");
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
                    case "action":
                        if (args.length < 2) {
                            Bukkit.dispatchCommand(sender, "fakeplayers");
                        } else {
                            if (args.length >= 3) {
                                if (!args[1].equalsIgnoreCase("All")) {
                                    FakePlayer player = FakePlayer.getFakePlayer(args[1]);

                                    if (player != null) {
                                        action(sender, player, args);
                                    } else {
                                        sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.invalid-player", args));
                                    }
                                } else {
                                    for (FakePlayer player : FakePlayer.getFakePlayers()) {
                                        action(sender, player, args);
                                    }
                                }
                            } else if (args[1].equals("help")) {
                                sender.sendMessage("");
                                sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Action)&2]"));
                                sender.sendMessage("");
                                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) Chat (Message/Command) &b- makes fake player type a chat message"));
                                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) Teleport (Player Name) &b- teleports a fake player to other player's present position"));
                                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) Teleport (X) (Y) (Z) &b- teleports a fake player to specified XYZ coordinates"));
                                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) Attack &b- makes fake player attack the nearest entity"));
                                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) Interact &b- makes fake player right click on the nearest entity"));
                                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) perform &b- makes fake players perform all actions from their list"));
                                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) perform (Number) &b- makes fake players perform all actions from their list a certain amount of times"));
                                sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) clear &b- removes all fake player's actions"));
                                sender.sendMessage("");
                                sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Action)&2]"));
                                sender.sendMessage("");
                            } else {
                                Bukkit.dispatchCommand(sender, "fakeplayers");
                            }
                        }
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
                final int I = i;
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    new FakePlayer(UUID.randomUUID(), name + (I + 1), Bukkit.getServer().getWorlds().get(0).getSpawnLocation()).spawn();
                }, I * Main.config.getInt("tick-delay-between-joins"));
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
            Player player = Bukkit.getPlayer(name);
            if (player != null) {
                if (FakePlayer.getFakePlayer(name) != null) {
                    player.chat(message);
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
            List<FakePlayer> copy = new ArrayList<>(FakePlayer.getFakePlayers());
            for (FakePlayer player : copy) {
                player.removePlayer();
            }
            sender.sendMessage(Main.getConfigMessage(Main.config, "messages.disband.all-disbanded-success", args));
        } else {
            FakePlayer fakePlayer = FakePlayer.getFakePlayer(name);

            if (fakePlayer != null) {
                fakePlayer.removePlayer();
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

    private void action(CommandSender sender, FakePlayer player, String[] args) {
        switch (args[2].toLowerCase()) {
            case "perform":
                new Thread(() -> {
                    if (args.length == 3) {
                        player.perform(1);
                    } else {
                        player.perform(Integer.parseInt(args[3]));
                    }
                }).start();
                break;
            case "chat":
                StringBuilder message = new StringBuilder();

                for (int i = 3; i < args.length; i++) {
                    message.append(args[i]).append(" ");
                }

                player.addAction(new ActionChat(message.toString()));
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.action-success", args));
                break;
            case "teleport":
                if (args.length == 4) {
                    Player location = Bukkit.getPlayer(args[3]);

                    if (location == null) {
                        sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.invalid-player", args));
                    } else {
                        player.addAction(new ActionTeleport(location));
                        sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.action-success", args));
                    }
                } else if (args.length == 6) {
                    Location location = new Location(Bukkit.getPlayer(player.getUUID()).getWorld(), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));

                    player.addAction(new ActionTeleport(location));
                    sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.action-success", args));
                } else {
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Action: Teleport)&2]"));
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) Teleport (Player Name) &b- teleports a Fake Player to other player's present position"));
                    sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) Teleport (X) (Y) (Z) &b- teleports a Fake Player to specified XYZ coordinates"));
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Action: Teleport)&2]"));
                    sender.sendMessage("");
                }
                break;
            case "attack":
                player.addAction(new ActionAttack());
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.action-success", args));
                break;
            case "interact":
                player.addAction(new ActionInteract());
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.action-success", args));
                break;
            /*
            case "inventoryclick":
                if (args.length == 4) {
                    player.addAction(new ActionInventoryClick(Integer.parseInt(args[3])));
                    sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.action-success", args));
                } else {
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Action: InventoryClick)&2]"));
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) InventoryClick (Slot) &b- makes Fake Player click on a certain custom inventory slot"));
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Action: InventoryClick)&2]"));
                    sender.sendMessage("");
                }
                break;

            case "inventoryclose":
                player.addAction(new ActionInventoryClose());
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.action-success", args));
                break;
             */
            case "wait":
                if (args.length == 4) {
                    player.addAction(new ActionWait(Long.parseLong(args[3])));
                    sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.action-success", args));
                } else {
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Action: Wait)&2]"));
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2> &3/FakePlayers action (Name/All) Wait (Milliseconds) &b- makes Fake Player wait a certain amount of milliseconds"));
                    sender.sendMessage("");
                    sender.sendMessage(Color.format("&2[&aFake players - &bHelp (Action: Wait)&2]"));
                    sender.sendMessage("");
                }
                break;
            case "clear":
                player.getActions().clear();
                sender.sendMessage(Main.getConfigMessage(Main.config, "messages.action.actions-clear", args));
                break;
        }
    }
}
