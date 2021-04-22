package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActionTeleport implements Action {

    private Location teleportLocation = null;
    private Player player = null;

    public ActionTeleport(Location teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    public ActionTeleport(Player player) {
        this.player = player;
    }

    @Override
    public void perform(FakePlayer player) {

        Reflection reflection = Reflection.getInstance();

        if (reflection.packetPlayOutEntityTeleport == null) {
            try {
                reflection.entityPlayer = Reflection.getNMSClass("EntityPlayer");
                reflection.NMSEntity = Reflection.getNMSClass("Entity");

                reflection.chunkProviderServer = Reflection.getNMSClass("ChunkProviderServer");
                reflection.worldServer = Reflection.getNMSClass("WorldServer");
                reflection.packetPlayOutEntityTeleport = Reflection.getNMSClass("PacketPlayOutEntityTeleport");

                reflection.packet = Reflection.getNMSClass("Packet");

                try {
                    reflection.entityTracker = Reflection.getNMSClass("EntityTracker");
                } catch (ClassNotFoundException ignored) {

                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        Method getWorldServer = null;
        Method getChunkProvider = null;
        Method broadcastIncludingSelf = null;

        Method getTracker = null;
        Method sendPacketToEntity = null;

        try {
            broadcastIncludingSelf = reflection.chunkProviderServer.getMethod("broadcastIncludingSelf", reflection.NMSEntity, reflection.packet);
            getChunkProvider = reflection.worldServer.getMethod("getChunkProvider");
            getWorldServer = reflection.entityPlayer.getMethod("getWorldServer");
        } catch (NoSuchMethodException e) {
            try {
                getWorldServer = reflection.entityPlayer.getMethod("u");
                getTracker = reflection.worldServer.getMethod("getTracker");
                sendPacketToEntity = reflection.entityTracker.getMethod("sendPacketToEntity", reflection.NMSEntity, reflection.packet);
            } catch (NoSuchMethodException ignored) {

            }
        }

        Player p = Bukkit.getPlayer(player.getUUID());

        if (teleportLocation != null) {
            p.teleport(teleportLocation);
        } else {
            p.teleport(this.player.getLocation());
        }

        try {
            Object entityPlayer = reflection.entityPlayer.cast(player.getEntityPlayer());

            Object teleport = reflection.packetPlayOutEntityTeleport.getConstructor(reflection.NMSEntity).newInstance(entityPlayer);

            Object chunkproviderserver;

            if (getChunkProvider != null) {
                chunkproviderserver = getChunkProvider.invoke(getWorldServer.invoke(entityPlayer));
                broadcastIncludingSelf.invoke(chunkproviderserver, entityPlayer, teleport);
            } else {
                Object entityTracker = getTracker.invoke(getWorldServer.invoke(entityPlayer));
                sendPacketToEntity.invoke(entityTracker, entityPlayer, teleport);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ActionType getType() {
        return ActionType.TELEPORT;
    }

    public Location getTeleportLocation() {
        return teleportLocation;
    }

    public Player getPlayer() {
        return player;
    }
}
