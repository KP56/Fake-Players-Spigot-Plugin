package me.KP56.FakePlayers.Action;

import me.KP56.FakePlayers.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class ActionAttack implements Action {

    private static Class<?> craftPlayer = null;
    private static Class<?> craftEntity = null;

    private static Class<?> entityPlayer = null;
    private static Class<?> NMSEntity = null;

    private static Class<?> getCraftBukkitClass(String craftBukkitClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + craftBukkitClassString;
        Class<?> craftBukkitClass = Class.forName(name);
        return craftBukkitClass;
    }

    private static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    @Override
    public void perform(FakePlayer player) {

        if (craftPlayer == null || craftEntity == null) {
            try {
                craftPlayer = getCraftBukkitClass("entity.CraftPlayer");
                craftEntity = getCraftBukkitClass("entity.CraftEntity");
                entityPlayer = getNMSClass("EntityPlayer");
                NMSEntity = getNMSClass("Entity");
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
            p.teleport(p.getLocation().setDirection(player.getLocation().subtract(closestEntity.getLocation()).toVector()));

            Method cPlayerGetHandle = null;
            Method cEntityGetHandle = null;
            Method entityPlayerAttack = null;

            try {
                cPlayerGetHandle = craftPlayer.getMethod("getHandle");
                cEntityGetHandle = craftEntity.getMethod("getHandle");
                entityPlayerAttack = entityPlayer.getMethod("attack", NMSEntity);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            Object cPlayer = craftPlayer.cast(p);
            Object cEntity = craftEntity.cast(closestEntity);

            try {
                Object entityPlayer = cPlayerGetHandle.invoke(cPlayer);
                Object entity = cEntityGetHandle.invoke(cEntity);
                entityPlayerAttack.invoke(entityPlayer, entity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
