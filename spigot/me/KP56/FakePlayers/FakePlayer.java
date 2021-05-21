package me.KP56.FakePlayers;

import me.KP56.FakePlayers.Action.Action;
import me.KP56.FakePlayers.Action.ActionWait;
import me.KP56.FakePlayers.MultiVersion.Version;
import me.KP56.FakePlayers.MultiVersion.v1_12_R1;
import me.KP56.FakePlayers.MultiVersion.v1_16_R3;
import me.KP56.FakePlayers.MultiVersion.v1_8_R3;
import me.KP56.FakePlayers.PluginUtils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayer {

    private static List<FakePlayer> fakePlayers = new ArrayList<>();
    public List<Action> actions = new ArrayList<>();
    private UUID uuid;
    private String name;
    private Object entityPlayer;

    public FakePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
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

    public static boolean summon(String name) {
        return new FakePlayer(Main.getRandomUUID(name), name).spawn();
    }

    public boolean spawn() {

        List<HandlerList> copy = new ArrayList<>(HandlerList.getHandlerLists());
        if (Main.getPlugin().usesProtocolLib()) {
            ProtocolLibUtils.unregisterHandlers();
        }

        if (Main.getPlugin().usesFastLogin()) {
            FastLoginUtils.unregisterHandlers();
        }

        if (Main.getPlugin().usesAuthMe()) {
            AuthMeUtils.unregisterHandlers();
        }

        if (Main.getPlugin().usesHamsterAPI()) {
            HamsterAPIUtils.unregisterHandlers();
        }

        if (Main.getPlugin().usesNexEngine()) {
            NexEngineUtils.unregisterHandlers();
        }

        if (Main.getPlugin().usesCustomDisplay()) {
            CustomDisplayUtils.unregisterHandlers();
        }

        if (name.length() >= 16) {
            return false;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }

        if (Main.getPlugin().getVersion() == Version.v1_16_R3) {
            entityPlayer = v1_16_R3.spawn(this);
        } else if (Main.getPlugin().getVersion() == Version.v1_12_R1) {
            entityPlayer = v1_12_R1.spawn(this);
        } else if (Main.getPlugin().getVersion() == Version.v1_8_R3) {
            entityPlayer = v1_8_R3.spawn(this);
        }

        fakePlayers.add(this);

        if (Main.getPlugin().usesProtocolLib() || Main.getPlugin().usesFastLogin() || Main.getPlugin().usesAuthMe() ||
                Main.getPlugin().usesHamsterAPI() || Main.getPlugin().usesNexEngine() || Main.getPlugin().usesCustomDisplay()) {
            try {
                Field field = HandlerList.class.getDeclaredField("allLists");
                field.setAccessible(true);
                field.set(null, copy);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Object getEntityPlayer() {
        return entityPlayer;
    }

    public String getName() {
        return name;
    }

    public void removePlayer() {
        if (Main.getPlugin().getVersion() == Version.v1_16_R3) {
            v1_16_R3.removePlayer(this);
        } else if (Main.getPlugin().getVersion() == Version.v1_12_R1) {
            v1_12_R1.removePlayer(this);
        } else if (Main.getPlugin().getVersion() == Version.v1_8_R3) {
            v1_8_R3.removePlayer(this);
        }
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public void perform(int number) {
        for (int i = 0; i < number; i++) {
            for (Action action : actions) {
                if (!(action instanceof ActionWait)) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> action.perform(this), 0);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    action.perform(this);
                }
            }
        }
    }

    public List<Action> getActions() {
        return actions;
    }
}
