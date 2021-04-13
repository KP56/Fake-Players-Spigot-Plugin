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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public final class v1_12_R1 {
    public static EntityPlayer spawn(FakePlayer fakePlayer) {
        final WorldServer WORLD_SERVER = ((CraftWorld) fakePlayer.getLocation().getWorld()).getHandle();
        final Location LOCATION = fakePlayer.getLocation();

        final MinecraftServer MC_SERVER = ((CraftServer) Bukkit.getServer()).getServer();

        EntityPlayer entityPlayer = createEntityPlayer(fakePlayer.getUUID(), fakePlayer.getName(), LOCATION);

        entityPlayer.setLocation(LOCATION.getX(), LOCATION.getY(), LOCATION.getZ(), LOCATION.getYaw(), LOCATION.getPitch());

        DataWatcher data = entityPlayer.getDataWatcher();
        data.set(DataWatcherRegistry.a.a(13), (byte) 127);

        final String JOIN_MESSAGE = getJoinMessage(entityPlayer);

        Player bukkitPlayer = entityPlayer.getBukkitEntity();

        if (me.KP56.FakePlayers.Main.usesPaper()) {
            PaperUtils_v1_12_R1.playerInitialSpawnEvent(bukkitPlayer);
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

        WORLD_SERVER.getPlayerChunkMap().addPlayer(entityPlayer);
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

            Method playersByNamePut = Map.class.getDeclaredMethod("put", Object.class, Object.class);
            playersByNamePut.invoke(valPlayersByName, entityPlayer.getName(), entityPlayer);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }


        PlayerJoinEvent playerJoinEvent;

        playerJoinEvent = new PlayerJoinEvent(((CraftServer) Bukkit.getServer()).getPlayer(entityPlayer),  JOIN_MESSAGE);

        Bukkit.getPluginManager().callEvent(playerJoinEvent);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(playerJoinEvent.getJoinMessage());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        }

        WORLD_SERVER.addEntity(entityPlayer);

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
        final WorldServer WORLD_SERVER = ((CraftWorld) player.getLocation().getWorld()).getHandle();

        EntityPlayer entityPlayer = (EntityPlayer) player.getEntityPlayer();

        if (entityPlayer.activeContainer != entityPlayer.defaultContainer) {
            entityPlayer.closeInventory();
        }

         PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(CSERVER.getPlayer(entityPlayer), "§e" + entityPlayer.getName() + " left the game");


        Bukkit.getPluginManager().callEvent(playerQuitEvent);

        WORLD_SERVER.getPlayerChunkMap().removePlayer(entityPlayer);
        WORLD_SERVER.removeEntity(entityPlayer);

        if (MC_SERVER.isMainThread()) {
            entityPlayer.playerTick();
        }

        if (!entityPlayer.inventory.getCarried().isEmpty()) {
            ItemStack carried = entityPlayer.inventory.getCarried();
            entityPlayer.drop(carried, false);
        }

        entityPlayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());
        entityPlayer.getAdvancementData().a();
        MC_SERVER.getPlayerList().players.remove(entityPlayer);

        try {
            Field j = PlayerList.class.getDeclaredField("j");
            j.setAccessible(true);
            Object valJ = j.get(MC_SERVER.getPlayerList());

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
        final GameProfile GAMEPROFILE = entityPlayer.getProfile();
        final UserCache USERCACHE = ((CraftServer) Bukkit.getServer()).getServer().getUserCache();
        final GameProfile GAMEPROFILE_2 = USERCACHE.getProfile(entityPlayer.getName());

        final String s = GAMEPROFILE_2 == null ? GAMEPROFILE.getName() : GAMEPROFILE_2.getName();

        String joinMessage;
        if (entityPlayer.getProfile().getName().equalsIgnoreCase(s)) {
            joinMessage = "§e" + LocaleI18n.a("multiplayer.player.joined", entityPlayer.getName());
        } else {
            joinMessage = "§e" + LocaleI18n.a("multiplayer.player.joined.renamed", entityPlayer.getName(), s);
        }

        return joinMessage;
    }
}
