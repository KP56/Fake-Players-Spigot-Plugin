package me.KP56.FakePlayers.Utils;

import org.bukkit.ChatColor;

public class Color {
    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
