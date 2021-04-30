package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Reflection.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ActionAttack implements Action {

    @Override
    public void perform(FakePlayer player) {

        Reflection reflection = Reflection.getInstance();

        if (reflection.craftPlayer == null) {
            try {
                reflection.craftPlayer = Reflection.getCraftBukkitClass("entity.CraftPlayer");
                reflection.craftEntity = Reflection.getCraftBukkitClass("entity.CraftEntity");
                reflection. entityPlayer = Reflection.getNMSClass("EntityPlayer");
                reflection.NMSEntity = Reflection.getNMSClass("Entity");

                reflection.chunkProviderServer = Reflection.getNMSClass("ChunkProviderServer");
                reflection.worldServer = Reflection.getNMSClass("WorldServer");
                reflection.packetPlayOutAnimation = Reflection.getNMSClass("PacketPlayOutAnimation");

                reflection.packet = Reflection.getNMSClass("Packet");

                try {
                    reflection.entityTracker = Reflection.getNMSClass("EntityTracker");
                } catch (ClassNotFoundException ignored) {

                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        Player p = Bukkit.getPlayer(player.getUUID());

        List<Entity> near = p.getNearbyEntities(3, 3, 3);

        Entity closestEntity = null;
        double lowestDistance = Double.MAX_VALUE;

        for (Entity entity : near) {
            double distance = entity.getLocation().distance(p.getLocation());
            if (distance < lowestDistance) {
                if (!(entity instanceof Player)) {
                    lowestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        if (closestEntity != null) {
            p.teleport(p.getLocation().setDirection(p.getLocation().subtract(closestEntity.getLocation()).toVector()));

            Method cPlayerGetHandle = null;
            Method cEntityGetHandle = null;
            Method entityPlayerAttack = null;
            Method getWorldServer = null;
            Method getChunkProvider = null;
            Method broadcastIncludingSelf = null;

            Method getTracker = null;
            Method sendPacketToEntity = null;

            try {
                cPlayerGetHandle = reflection.craftPlayer.getMethod("getHandle");
                cEntityGetHandle = reflection.craftEntity.getMethod("getHandle");
                entityPlayerAttack = reflection.entityPlayer.getMethod("attack", reflection.NMSEntity);
                broadcastIncludingSelf = reflection.chunkProviderServer.getMethod("broadcastIncludingSelf", reflection.NMSEntity, reflection.packet);
                getChunkProvider = reflection.worldServer.getMethod("getChunkProvider");
                getWorldServer = reflection.entityPlayer.getMethod("getWorldServer");
            } catch (NoSuchMethodException e) {
                try {
                    getWorldServer = reflection.entityPlayer.getMethod("u");
                    getTracker = reflection.worldServer.getMethod("getTracker");
                    sendPacketToEntity = reflection.entityTracker.getMethod("sendPacketToEntity", reflection.NMSEntity, reflection.packet);
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
            }

            Object cPlayer = reflection.craftPlayer.cast(p);
            Object cEntity = reflection.craftEntity.cast(closestEntity);

            try {
                Object entityPlayer = cPlayerGetHandle.invoke(cPlayer);
                Object entity = cEntityGetHandle.invoke(cEntity);
                entityPlayerAttack.invoke(entityPlayer, entity);

                Object animation = reflection.packetPlayOutAnimation.getConstructor(reflection.NMSEntity, int.class).newInstance(reflection.NMSEntity.cast(entityPlayer), 0);

                Object chunkproviderserver;

                if (getChunkProvider != null) {
                    chunkproviderserver = getChunkProvider.invoke(getWorldServer.invoke(entityPlayer));
                    broadcastIncludingSelf.invoke(chunkproviderserver, entityPlayer, animation);
                } else {
                    Object entityTracker = getTracker.invoke(getWorldServer.invoke(entityPlayer));
                    sendPacketToEntity.invoke(entityTracker, entityPlayer, animation);
                }



            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public ActionType getType() {
        return ActionType.ATTACK;
    }
}
