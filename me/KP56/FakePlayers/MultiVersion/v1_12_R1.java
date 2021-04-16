package me.KP56.FakePlayers.MultiVersion;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Main;
import me.KP56.FakePlayers.Paper.PaperUtils_v1_12_R1;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;

public final class v1_12_R1 {
    public static EntityPlayer spawn(FakePlayer fakePlayer) {

        try {
            PlayerPreLoginEvent preLoginEvent = new PlayerPreLoginEvent(fakePlayer.getName(), InetAddress.getByName("127.0.0.1"), fakePlayer.getUUID());
            AsyncPlayerPreLoginEvent asyncPreLoginEvent = new AsyncPlayerPreLoginEvent(fakePlayer.getName(), InetAddress.getByName("127.0.0.1"), fakePlayer.getUUID());

            Bukkit.getPluginManager().callEvent(preLoginEvent);

            new Thread(() -> Bukkit.getPluginManager().callEvent(asyncPreLoginEvent)).start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        WorldServer worldServer = ((CraftWorld) fakePlayer.getLocation().getWorld()).getHandle();
        Location location = fakePlayer.getLocation();

        MinecraftServer mcServer = ((CraftServer) Bukkit.getServer()).getServer();

        EntityPlayer entityPlayer = createEntityPlayer(fakePlayer.getUUID(), fakePlayer.getName(), location);

        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        DataWatcher data = entityPlayer.getDataWatcher();
        data.set(DataWatcherRegistry.a.a(13), (byte) 127);

        String joinMessage = getJoinMessage(entityPlayer);

        Player bukkitPlayer = entityPlayer.getBukkitEntity();

        if (me.KP56.FakePlayers.Main.usesPaper()) {
            PaperUtils_v1_12_R1.playerInitialSpawnEvent(bukkitPlayer);
        }

        entityPlayer.spawnIn(worldServer);
        entityPlayer.playerInteractManager.a((WorldServer) entityPlayer.world);
        GameMode gamemode = Bukkit.getServer().getDefaultGameMode();
        if (gamemode == GameMode.SURVIVAL) {
            entityPlayer.playerInteractManager.b(EnumGamemode.SURVIVAL);
        } else if (gamemode == GameMode.CREATIVE) {
            entityPlayer.playerInteractManager.b(EnumGamemode.CREATIVE);
        } else if (gamemode == GameMode.ADVENTURE) {
            entityPlayer.playerInteractManager.b(EnumGamemode.ADVENTURE);
        } else if (gamemode == GameMode.SPECTATOR) {
            entityPlayer.playerInteractManager.b(EnumGamemode.SURVIVAL);
        }

        entityPlayer.playerConnection = new PlayerConnection(mcServer, new NetworkManager(EnumProtocolDirection.CLIENTBOUND), entityPlayer);

        entityPlayer.playerConnection.networkManager.channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        entityPlayer.playerConnection.networkManager.channel.close();

        worldServer.getPlayerChunkMap().addPlayer(entityPlayer);
        mcServer.getPlayerList().players.add(entityPlayer);
        try {
            Field j = PlayerList.class.getDeclaredField("j");
            j.setAccessible(true);
            Object valJ = j.get(mcServer.getPlayerList());

            Method jPut = valJ.getClass().getDeclaredMethod("put", Object.class, Object.class);
            jPut.invoke(valJ, bukkitPlayer.getUniqueId(), entityPlayer);

            Field playersByName = PlayerList.class.getDeclaredField("playersByName");
            playersByName.setAccessible(true);
            Object valPlayersByName = playersByName.get(mcServer.getPlayerList());

            Method playersByNamePut = Map.class.getDeclaredMethod("put", Object.class, Object.class);
            playersByNamePut.invoke(valPlayersByName, entityPlayer.getName(), entityPlayer);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }


        PlayerJoinEvent playerJoinEvent;

        playerJoinEvent = new PlayerJoinEvent(((CraftServer) Bukkit.getServer()).getPlayer(entityPlayer),  joinMessage);

        Bukkit.getPluginManager().callEvent(playerJoinEvent);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(playerJoinEvent.getJoinMessage());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        }

        worldServer.addEntity(entityPlayer);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), entityPlayer::playerTick, 1, 1);

        return entityPlayer;
    }

    private static EntityPlayer createEntityPlayer(UUID uuid, String name, Location location) {
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        MinecraftServer mcServer = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile gameProfile = new GameProfile(uuid, name);

        return new EntityPlayer(mcServer, worldServer, gameProfile, new PlayerInteractManager(worldServer));
    }

    public static void removePlayer(FakePlayer player) {
        MinecraftServer mcServer = ((CraftServer) Bukkit.getServer()).getServer();
        CraftServer cserver = (CraftServer) Bukkit.getServer();
        WorldServer worldServer = ((CraftWorld) player.getLocation().getWorld()).getHandle();

        EntityPlayer entityPlayer = (EntityPlayer) player.getEntityPlayer();

        if (entityPlayer.activeContainer != entityPlayer.defaultContainer) {
            entityPlayer.closeInventory();
        }

         PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(cserver.getPlayer(entityPlayer), "§e" + entityPlayer.getName() + " left the game");


        Bukkit.getPluginManager().callEvent(playerQuitEvent);

        worldServer.getPlayerChunkMap().removePlayer(entityPlayer);
        worldServer.removeEntity(entityPlayer);

        if (mcServer.isMainThread()) {
            entityPlayer.playerTick();
        }

        if (!entityPlayer.inventory.getCarried().isEmpty()) {
            ItemStack carried = entityPlayer.inventory.getCarried();
            entityPlayer.drop(carried, false);
        }

        entityPlayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        entityPlayer.getAdvancementData().a();
        mcServer.getPlayerList().players.remove(entityPlayer);

        try {
            Field j = PlayerList.class.getDeclaredField("j");
            j.setAccessible(true);
            Object valJ = j.get(mcServer.getPlayerList());

            Method jRemove = valJ.getClass().getDeclaredMethod("remove", Object.class);
            jRemove.invoke(valJ, entityPlayer.getUniqueID());
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

    private static String getJoinMessage(EntityPlayer entityPlayer) {
        GameProfile gameProfile = entityPlayer.getProfile();
        UserCache userCache = ((CraftServer) Bukkit.getServer()).getServer().getUserCache();
        GameProfile gameprofile2 = userCache.getProfile(entityPlayer.getName());

        String s = gameprofile2 == null ? gameProfile.getName() : gameprofile2.getName();

        String joinMessage;
        if (entityPlayer.getProfile().getName().equalsIgnoreCase(s)) {
            joinMessage = "§e" + LocaleI18n.a("multiplayer.player.joined", entityPlayer.getName());
        } else {
            joinMessage = "§e" + LocaleI18n.a("multiplayer.player.joined.renamed", entityPlayer.getName(), s);
        }

        return joinMessage;
    }
}
