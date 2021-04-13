package me.KP56.FakePlayers.MultiVersion;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.embedded.EmbeddedChannel;
import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Main;
import me.KP56.FakePlayers.Paper.PaperUtils_v1_16_R3;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;

public final class v1_16_R3 {
    public static EntityPlayer spawn(FakePlayer fakePlayer) {
        final WorldServer WORLD_SERVER = ((CraftWorld) fakePlayer.getLocation().getWorld()).getHandle();
        final Location LOCATION = fakePlayer.getLocation();

        final MinecraftServer MC_SERVER = ((CraftServer) Bukkit.getServer()).getServer();

        EntityPlayer entityPlayer = createEntityPlayer(fakePlayer.getUUID(), fakePlayer.getName(), LOCATION);

        entityPlayer.setLocation(LOCATION.getX(), LOCATION.getY(), LOCATION.getZ(), LOCATION.getYaw(), LOCATION.getPitch());

        DataWatcher data = entityPlayer.getDataWatcher();
        data.set(DataWatcherRegistry.a.a(16), (byte) 127);

        final ChatMessage JOIN_MESSAGE = getJoinMessage(entityPlayer);

        Player bukkitPlayer = entityPlayer.getBukkitEntity();

        if (me.KP56.FakePlayers.Main.usesPaper() && Bukkit.getVersion().contains("1.16.5")) {
            PaperUtils_v1_16_R3.playerInitialSpawnEvent(bukkitPlayer);
        }

        entityPlayer.spawnIn(WORLD_SERVER);
        entityPlayer.playerInteractManager.a((WorldServer) entityPlayer.world);
        final GameMode GAMEMODE = Bukkit.getServer().getDefaultGameMode();
        if (GAMEMODE == GameMode.SURVIVAL) {
            entityPlayer.playerInteractManager.b(EnumGamemode.SURVIVAL);
        } else if (GAMEMODE == GameMode.CREATIVE) {
            entityPlayer.playerInteractManager.b(EnumGamemode.CREATIVE);
        } else if (GAMEMODE == GameMode.ADVENTURE) {
            entityPlayer.playerInteractManager.b(EnumGamemode.ADVENTURE);
        } else if (GAMEMODE == GameMode.SPECTATOR) {
            entityPlayer.playerInteractManager.b(EnumGamemode.SURVIVAL);
        }

        entityPlayer.playerConnection = new PlayerConnection(MC_SERVER, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), entityPlayer);
        entityPlayer.playerConnection.networkManager.channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());

        entityPlayer.playerConnection.networkManager.channel.close();

        WORLD_SERVER.addPlayerJoin(entityPlayer);
        MC_SERVER.getPlayerList().players.add(entityPlayer);
        try {
            Field j = PlayerList.class.getDeclaredField("j");
            j.setAccessible(true);
            Object valJ = j.get(MC_SERVER.getPlayerList());

            Method jPut = valJ.getClass().getDeclaredMethod("put", Object.class, Object.class);
            jPut.invoke(valJ, bukkitPlayer.getUniqueId(), entityPlayer);

            Field playersByName = PlayerList.class.getDeclaredField("playersByName");
            playersByName.setAccessible(true);
            Object valPlayersByName = playersByName.get(MC_SERVER.getPlayerList());

            Method playersByNamePut = valPlayersByName.getClass().getDeclaredMethod("put", Object.class, Object.class);
            playersByNamePut.invoke(valPlayersByName, entityPlayer.getName().toLowerCase(Locale.ROOT), entityPlayer);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }


        PlayerJoinEvent playerJoinEvent;
        if (me.KP56.FakePlayers.Main.usesPaper() && Bukkit.getVersion().contains("1.16.5")) {
            playerJoinEvent = PaperUtils_v1_16_R3.paperJoinMessageFormat(entityPlayer, JOIN_MESSAGE);
        } else {
            playerJoinEvent = new PlayerJoinEvent(((CraftServer) Bukkit.getServer()).getPlayer(entityPlayer),  CraftChatMessage.fromComponent(JOIN_MESSAGE));
        }

        Bukkit.getPluginManager().callEvent(playerJoinEvent);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(playerJoinEvent.getJoinMessage());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), entityPlayer::playerTick, 1, 1);

        return entityPlayer;
    }

    private static EntityPlayer createEntityPlayer(UUID uuid, String name, Location location) {
        final WorldServer WORLD_SERVER = ((CraftWorld) location.getWorld()).getHandle();
        final MinecraftServer MC_SERVER = ((CraftServer) Bukkit.getServer()).getServer();
        final GameProfile GAME_PROFILE = new GameProfile(uuid, name);

        return new EntityPlayer(MC_SERVER, WORLD_SERVER, GAME_PROFILE, new PlayerInteractManager(WORLD_SERVER));
    }

    public static void removePlayer(FakePlayer player) {
        final MinecraftServer MC_SERVER = ((CraftServer) Bukkit.getServer()).getServer();
        final CraftServer CSERVER = (CraftServer) Bukkit.getServer();

        EntityPlayer entityPlayer = (EntityPlayer) player.getEntityPlayer();

        final WorldServer WORLD_SERVER = entityPlayer.getWorld().getWorld().getHandle();

        entityPlayer.a(StatisticList.LEAVE_GAME);

        if (entityPlayer.activeContainer != entityPlayer.defaultContainer) {
            entityPlayer.closeInventory();
        }

        PlayerQuitEvent playerQuitEvent;
        if (Main.usesPaper()) {
            playerQuitEvent = PaperUtils_v1_16_R3.paperQuitMessageFormat(entityPlayer, CSERVER.getPlayer(entityPlayer));
        } else {
            playerQuitEvent = new PlayerQuitEvent(CSERVER.getPlayer(entityPlayer), "§e" + entityPlayer.getName() + " left the game");
        }


        Bukkit.getPluginManager().callEvent(playerQuitEvent);

        entityPlayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        if (MC_SERVER.isMainThread()) {
            entityPlayer.playerTick();
        }

        if (!entityPlayer.inventory.getCarried().isEmpty()) {
            ItemStack carried = entityPlayer.inventory.getCarried();
            entityPlayer.drop(carried, false);
        }

        entityPlayer.decouple();
        WORLD_SERVER.removePlayer(entityPlayer);
        entityPlayer.getAdvancementData().a();
        MC_SERVER.getPlayerList().players.remove(entityPlayer);

        try {
            Field j = PlayerList.class.getDeclaredField("j");
            j.setAccessible(true);
            Object valJ = j.get(MC_SERVER.getPlayerList());

            Method jRemove = valJ.getClass().getDeclaredMethod("remove", Object.class);
            jRemove.invoke(valJ, entityPlayer.getUniqueID());

            Field playersByName = PlayerList.class.getDeclaredField("playersByName");
            playersByName.setAccessible(true);
            Object valPlayersByName = playersByName.get(MC_SERVER.getPlayerList());

            Method playersByNameRemove = valPlayersByName.getClass().getDeclaredMethod("remove", Object.class);
            playersByNameRemove.invoke(valPlayersByName, entityPlayer.getName().toLowerCase(Locale.ROOT));
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        FakePlayer.getFakePlayers().remove(player);

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()));
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));

            p.sendMessage(playerQuitEvent.getQuitMessage());
        }
    }

    private static ChatMessage getJoinMessage(EntityPlayer entityPlayer) {
        final GameProfile GAMEPROFILE = entityPlayer.getProfile();
        final UserCache USERCACHE = ((CraftServer) Bukkit.getServer()).getServer().getUserCache();
        final GameProfile GAMEPROFILE_2 = USERCACHE.getProfile(GAMEPROFILE.getId());

        final String s = GAMEPROFILE_2 == null ? GAMEPROFILE.getName() : GAMEPROFILE_2.getName();

        ChatMessage chatMessage;
        if (entityPlayer.getProfile().getName().equalsIgnoreCase(s)) {
            chatMessage = new ChatMessage("multiplayer.player.joined", entityPlayer.getScoreboardDisplayName());
        } else {
            chatMessage = new ChatMessage("multiplayer.player.joined.renamed", entityPlayer.getScoreboardDisplayName(), s);
        }

        chatMessage.a(EnumChatFormat.YELLOW);

        return chatMessage;
    }
}
