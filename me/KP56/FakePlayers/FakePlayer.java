package me.KP56.FakePlayers;

import me.KP56.FakePlayers.Action.Action;
import me.KP56.FakePlayers.Action.ActionWait;
import me.KP56.FakePlayers.MultiVersion.Version;
import me.KP56.FakePlayers.MultiVersion.v1_12_R1;
import me.KP56.FakePlayers.MultiVersion.v1_16_R3;
import me.KP56.FakePlayers.MultiVersion.v1_8_R3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePlayer {

    private static List<FakePlayer> fakePlayers = new ArrayList<>();

    private UUID uuid;
    private String name;
    private Location location;

    private Object entityPlayer;

    private List<Action> actions = new ArrayList<>();

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

    public boolean spawn() {
        if (name.length() >= 16) {
            return false;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }

        if (Main.getVersion() == Version.v1_16_R3) {
            entityPlayer = v1_16_R3.spawn(this);
        } else if (Main.getVersion() == Version.v1_12_R1) {
            entityPlayer = v1_12_R1.spawn(this);
        } else if (Main.getVersion() == Version.v1_8_R3) {
            entityPlayer = v1_8_R3.spawn(this);
        }


        fakePlayers.add(this);

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
        if (Main.getVersion() == Version.v1_16_R3) {
            v1_16_R3.removePlayer(this);
        } else if (Main.getVersion() == Version.v1_12_R1) {
            v1_12_R1.removePlayer(this);
        } else if (Main.getVersion() == Version.v1_8_R3) {
            v1_8_R3.removePlayer(this);
        }
    }

    public Location getLocation() {
        return location;
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
