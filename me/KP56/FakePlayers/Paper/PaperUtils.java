package me.KP56.FakePlayers.Paper;

import com.destroystokyo.paper.PaperConfig;
import com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.v1_16_R3.ChatMessage;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public final class PaperUtils {
    public static void playerInitialSpawnEvent(Player p) {
        PlayerSpawnLocationEvent ev = new PlayerInitialSpawnEvent(p, p.getLocation());
        Bukkit.getPluginManager().callEvent(ev);
    }

    public static PlayerJoinEvent paperJoinMessageFormat(EntityPlayer player, ChatMessage message) {
        return new PlayerJoinEvent(((CraftServer) Bukkit.getServer()).getPlayer(player), PaperAdventure.asAdventure(message));
    }

    public static PlayerQuitEvent paperQuitMessageFormat(EntityPlayer entityPlayer, Player player) {
        return new PlayerQuitEvent(player, Component.translatable("multiplayer.player.left", NamedTextColor.YELLOW, PaperConfig.useDisplayNameInQuit ? player.displayName() : Component.text(player.getName())), entityPlayer.quitReason);
    }
}
