package me.KP56.FakePlayers.Reflection;

import org.bukkit.Bukkit;

public class Reflection {

    public Class<?> craftPlayer = null;
    public Class<?> craftEntity = null;

    public Class<?> entityPlayer = null;
    public Class<?> NMSEntity = null;

    public Class<?> chunkProviderServer = null;
    public Class<?> worldServer = null;
    public Class<?> packetPlayOutAnimation = null;
    public Class<?> packetPlayOutEntityTeleport = null;
    public Class<?> packet = null;

    public Class<?> entityTracker = null;

    public static Class<?> getCraftBukkitClass(String craftBukkitClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "org.bukkit.craftbukkit." + version + craftBukkitClassString;
        Class<?> craftBukkitClass = Class.forName(name);
        return craftBukkitClass;
    }

    public static Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    private Reflection() {}

    private static Reflection reflection = new Reflection();

    public static Reflection getInstance() {
        return reflection;
    }
}
