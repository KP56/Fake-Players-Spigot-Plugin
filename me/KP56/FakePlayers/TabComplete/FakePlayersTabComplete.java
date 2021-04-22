package me.KP56.FakePlayers.TabComplete;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FakePlayersTabComplete implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> subCommands = new ArrayList<>();

        if (args[0].equals("action")) {
            if (args.length == 1) {
                subCommands.add("help");
            } else if (args.length == 2) {
                subCommands.add("chat");
                subCommands.add("teleport");
                subCommands.add("attack");
                subCommands.add("interact");
                subCommands.add("inventoryclick");
                subCommands.add("inventoryclose");
                subCommands.add("perform");
                subCommands.add("clear");
            }
        } else {
            subCommands.add("summon");
            subCommands.add("disband");
            subCommands.add("chat");
            subCommands.add("action");
        }

        subCommands.add("list");
        subCommands.add("reload");
        subCommands.add("macro");
        subCommands.add("macro save");
        subCommands.add("macro load");
        subCommands.add("macro perform");

        return subCommands;
    }
}
