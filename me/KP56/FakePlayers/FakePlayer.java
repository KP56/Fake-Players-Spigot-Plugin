package me.KP56.FakePlayers;

import com.mojang.authlib.GameProfile;
import me.KP56.FakePlayers.Paper.PaperUtils;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class FakePlayer {

    private static List<FakePlayer> fakePlayers = new ArrayList<>();

    private UUID uuid;
    private String name;
    private Location location;

    private EntityPlayer entityPlayer;

    private int taskID;

    public FakePlayer(UUID uuid, String name, Location location) {
        this.uuid = uuid;
        this.name = name;
        this.location = location;
    }

    public static FakePlayer getFakePlayer(UUID uuid) {
        for (FakePlayer player : fakePlayers) {
            if (player.getUUID() == uuid) {
                return player;
            }
        }

        return null;
    }

    public static FakePlayer getFakePlayer(String name) {
        for (FakePlayer player : fakePlayers) {
            if (player.getName().equals(name)) {
                return player;
            }
        }

        return null;
    }

    public static int getAmount() {
        return fakePlayers.size();
    }

    public static List<FakePlayer> getFakePlayers() {
        return fakePlayers;
    }

    private EntityPlayer createEntityPlayer() {
        final WorldServer WORLD_SERVER = ((CraftWorld) location.getWorld()).getHandle();
        final MinecraftServer MC_SERVER = ((CraftServer) Bukkit.getServer()).getServer();
        final GameProfile GAME_PROFILE = new GameProfile(uuid, name);

        return new EntityPlayer(MC_SERVER, WORLD_SERVER, GAME_PROFILE, new PlayerInteractManager(WORLD_SERVER));
    }

    public boolean spawn() {
        if (name.length() >= 16) {
            return false;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }

        final WorldServer WORLD_SERVER = ((CraftWorld) location.getWorld()).getHandle();
        final MinecraftServer MC_SERVER = ((CraftServer) Bukkit.getServer()).getServer();

        EntityPlayer entityPlayer = createEntityPlayer();

        entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        DataWatcher data = entityPlayer.getDataWatcher();
        data.set(DataWatcherRegistry.a.a(16), (byte) 127);

        final ChatMessage JOIN_MESSAGE = getJoinMessage(entityPlayer);

        Player bukkitPlayer = entityPlayer.getBukkitEntity();

        if (Main.usesPaper()) {
            PaperUtils.playerInitialSpawnEvent(bukkitPlayer);
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
        if (Main.usesPaper()) {
            playerJoinEvent = PaperUtils.paperJoinMessageFormat(entityPlayer, JOIN_MESSAGE);
        } else {
            playerJoinEvent = new PlayerJoinEvent(((CraftServer) Bukkit.getServer()).getPlayer(entityPlayer),  CraftChatMessage.fromComponent(JOIN_MESSAGE));
        }
        Bukkit.getPluginManager().callEvent(playerJoinEvent);

        this.entityPlayer = entityPlayer;

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(playerJoinEvent.getJoinMessage());
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        }

        fakePlayers.add(this);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), entityPlayer::playerTick, 1, 1);

        return true;
    }

    public UUID getUUID() {
        return uuid;
    }

    public EntityPlayer getEntityPlayer() {
        return entityPlayer;
    }

    public String getName() {
        return name;
    }

    public void removePlayer() {
        final MinecraftServer MC_SERVER = ((CraftServer) Bukkit.getServer()).getServer();
        final CraftServer CSERVER = (CraftServer) Bukkit.getServer();
        final WorldServer WORLD_SERVER = ((CraftWorld) location.getWorld()).getHandle();

        entityPlayer.a(StatisticList.LEAVE_GAME);

        if (entityPlayer.activeContainer != entityPlayer.defaultContainer) {
            entityPlayer.closeInventory(InventoryCloseEvent.Reason.DISCONNECT);
        }

        PlayerQuitEvent playerQuitEvent;
        if (Main.usesPaper()) {
            playerQuitEvent = PaperUtils.paperQuitMessageFormat(entityPlayer, CSERVER.getPlayer(entityPlayer));
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
            entityPlayer.inventory.setCarried(ItemStack.NULL_ITEM);
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

        FakePlayer.getFakePlayers().remove(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()));
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));

            p.sendMessage(playerQuitEvent.getQuitMessage());
        }
    }

    private ChatMessage getJoinMessage(EntityPlayer entityPlayer) {
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
