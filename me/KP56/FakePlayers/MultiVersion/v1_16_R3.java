package me.KP56.FakePlayers.MultiVersion;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Main;
import me.KP56.FakePlayers.Paper.PaperUtils_v1_16_R3;
import me.KP56.FakePlayers.Spigot.SpigotUtils;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.UUID;

public final class v1_16_R3 {
    public static EntityPlayer spawn(FakePlayer fakePlayer) {

        WorldServer worldServer = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();

        MinecraftServer mcServer = ((CraftServer) Bukkit.getServer()).getServer();

        EntityPlayer entityPlayer = createEntityPlayer(fakePlayer.getUUID(), fakePlayer.getName(), worldServer);

        CraftPlayer bukkitPlayer = entityPlayer.getBukkitEntity();

        try {
            PlayerPreLoginEvent preLoginEvent = new PlayerPreLoginEvent(fakePlayer.getName(), InetAddress.getByName("127.0.0.1"), fakePlayer.getUUID());
            AsyncPlayerPreLoginEvent asyncPreLoginEvent = new AsyncPlayerPreLoginEvent(fakePlayer.getName(), InetAddress.getByName("127.0.0.1"), fakePlayer.getUUID());

            new Thread(() -> Bukkit.getPluginManager().callEvent(asyncPreLoginEvent)).start();
            Bukkit.getPluginManager().callEvent(preLoginEvent);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        mcServer.getPlayerList().a(entityPlayer);

        Location loc = bukkitPlayer.getLocation();

        entityPlayer.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        DataWatcher data = entityPlayer.getDataWatcher();
        data.set(DataWatcherRegistry.a.a(16), (byte) 127);

        ChatMessage joinMessage = getJoinMessage(entityPlayer);

        if (me.KP56.FakePlayers.Main.getPlugin().usesPaper() && Main.getPlugin().isPaperUpdated()) {
            PaperUtils_v1_16_R3.playerInitialSpawnEvent(bukkitPlayer);
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

        worldServer.addPlayerJoin(entityPlayer);
        mcServer.getPlayerList().players.add(entityPlayer);
        try {
            Field j = PlayerList.class.getDeclaredField("j");
            j.setAccessible(true);
            Object valJ = j.get(mcServer.getPlayerList());

            Method jPut = valJ.getClass().getDeclaredMethod("put", Object.class, Object.class);
            jPut.invoke(valJ, bukkitPlayer.getUniqueId(), entityPlayer);

            if (!Main.getPlugin().usesCraftBukkit()) {
                Field playersByName = PlayerList.class.getDeclaredField("playersByName");
                playersByName.setAccessible(true);
                Object valPlayersByName = playersByName.get(mcServer.getPlayerList());

                Method playersByNamePut = valPlayersByName.getClass().getDeclaredMethod("put", Object.class, Object.class);
                playersByNamePut.invoke(valPlayersByName, entityPlayer.getName().toLowerCase(Locale.ROOT), entityPlayer);
            }
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        PlayerJoinEvent playerJoinEvent;
        if (me.KP56.FakePlayers.Main.getPlugin().usesPaper() && Main.getPlugin().isPaperUpdated()) {
            playerJoinEvent = PaperUtils_v1_16_R3.paperJoinMessageFormat(entityPlayer, joinMessage);
        } else {
            playerJoinEvent = new PlayerJoinEvent(((CraftServer) Bukkit.getServer()).getPlayer(entityPlayer), CraftChatMessage.fromComponent(joinMessage));
        }

        Bukkit.getPluginManager().callEvent(playerJoinEvent);

        try {
            Field didPlayerJoinEvent = entityPlayer.getClass().getDeclaredField("didPlayerJoinEvent");
            didPlayerJoinEvent.set(entityPlayer, true);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {

        }

        String finalJoinMessage = playerJoinEvent.getJoinMessage();

        if (finalJoinMessage != null && !finalJoinMessage.equals("")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(finalJoinMessage);
            }
        }

        PlayerResourcePackStatusEvent resourcePackStatusEventAccepted = new PlayerResourcePackStatusEvent(bukkitPlayer, PlayerResourcePackStatusEvent.Status.ACCEPTED);
        PlayerResourcePackStatusEvent resourcePackStatusEventSuccessfullyLoaded = new PlayerResourcePackStatusEvent(bukkitPlayer, PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (!Main.getPlugin().usesCraftBukkit()) {
                SpigotUtils.setResourcePackStatus(bukkitPlayer, PlayerResourcePackStatusEvent.Status.ACCEPTED);
            }
            Bukkit.getPluginManager().callEvent(resourcePackStatusEventAccepted);
        }, 20);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            if (!Main.getPlugin().usesCraftBukkit()) {
                SpigotUtils.setResourcePackStatus(bukkitPlayer, PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED);
            }
            Bukkit.getPluginManager().callEvent(resourcePackStatusEventSuccessfullyLoaded);
        }, 40);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityPlayer));
            connection.sendPacket(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), entityPlayer::playerTick, 1, 1);

        return entityPlayer;
    }

    private static EntityPlayer createEntityPlayer(UUID uuid, String name, WorldServer worldServer) {
        MinecraftServer mcServer = ((CraftServer) Bukkit.getServer()).getServer();
        GameProfile gameProfile = new GameProfile(uuid, name);

        return new EntityPlayer(mcServer, worldServer, gameProfile, new PlayerInteractManager(worldServer));
    }

    public static void removePlayer(FakePlayer player) {
        MinecraftServer mcServer = ((CraftServer) Bukkit.getServer()).getServer();
        CraftServer craftServer = (CraftServer) Bukkit.getServer();

        EntityPlayer entityPlayer = (EntityPlayer) player.getEntityPlayer();

        WorldServer worldServer = entityPlayer.getWorld().getWorld().getHandle();

        entityPlayer.a(StatisticList.LEAVE_GAME);

        if (entityPlayer.activeContainer != entityPlayer.defaultContainer) {
            entityPlayer.closeInventory();
        }

        PlayerQuitEvent playerQuitEvent;
        if (Main.getPlugin().usesPaper() && Main.getPlugin().isPaperUpdated()) {
            playerQuitEvent = PaperUtils_v1_16_R3.paperQuitMessageFormat(entityPlayer, craftServer.getPlayer(entityPlayer));
        } else {
            playerQuitEvent = new PlayerQuitEvent(craftServer.getPlayer(entityPlayer), "§e" + entityPlayer.getName() + " left the game");
        }


        Bukkit.getPluginManager().callEvent(playerQuitEvent);

        entityPlayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());

        if (mcServer.isMainThread()) {
            entityPlayer.playerTick();
        }

        if (!entityPlayer.inventory.getCarried().isEmpty()) {
            ItemStack carried = entityPlayer.inventory.getCarried();
            entityPlayer.drop(carried, false);
        }

        entityPlayer.decouple();
        worldServer.removePlayer(entityPlayer);
        entityPlayer.getAdvancementData().a();
        mcServer.getPlayerList().players.remove(entityPlayer);

        try {
            Field j = PlayerList.class.getDeclaredField("j");
            j.setAccessible(true);
            Object valJ = j.get(mcServer.getPlayerList());

            Method jRemove = valJ.getClass().getDeclaredMethod("remove", Object.class);
            jRemove.invoke(valJ, entityPlayer.getUniqueID());

            Field playersByName = PlayerList.class.getDeclaredField("playersByName");
            playersByName.setAccessible(true);
            Object valPlayersByName = playersByName.get(mcServer.getPlayerList());

            Method playersByNameRemove = valPlayersByName.getClass().getDeclaredMethod("remove", Object.class);
            playersByNameRemove.invoke(valPlayersByName, entityPlayer.getName().toLowerCase(Locale.ROOT));
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        FakePlayer.getFakePlayers().remove(player);

        String finalQuitMessage = playerQuitEvent.getQuitMessage();

        if (finalQuitMessage != null && !finalQuitMessage.equals("")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
                connection.sendPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()));
                connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityPlayer));

                p.sendMessage(playerQuitEvent.getQuitMessage());
            }
        }

        try {
            Method savePlayerFile = PlayerList.class.getDeclaredMethod("savePlayerFile", EntityPlayer.class);
            savePlayerFile.setAccessible(true);
            savePlayerFile.invoke(mcServer.getPlayerList(), entityPlayer);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static ChatMessage getJoinMessage(EntityPlayer entityPlayer) {
        GameProfile gameProfile = entityPlayer.getProfile();
        UserCache userCache = ((CraftServer) Bukkit.getServer()).getServer().getUserCache();
        GameProfile gameprofile2 = userCache.getProfile(gameProfile.getId());

        String s = gameprofile2 == null ? gameProfile.getName() : gameprofile2.getName();

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
