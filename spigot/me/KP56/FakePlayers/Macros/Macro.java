package me.KP56.FakePlayers.Macros;

import me.KP56.FakePlayers.Action.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Macro {
    private List<Action> actions;
    private String name;

    public Macro(List<Action> actions, String name) {
        this.actions = actions;
        this.name = name;
    }

    public List<Action> getActions() {
        return actions;
    }

    public String getName() {
        return name;
    }

    public void save() throws IOException {
        File newMacro = new File("plugins/FakePlayers/macros/" + name + ".fpmacro");

        FileOutputStream fos = new FileOutputStream(newMacro);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        for (Action action : actions) {
            if (action.getType() == ActionType.ATTACK) {
                bw.write("ATTACK");
            } else if (action.getType() == ActionType.CHAT) {
                bw.write("CHAT " + ((ActionChat) action).getMessage());
            } else if (action.getType() == ActionType.INTERACT) {
                bw.write("INTERACT");
            } else if (action.getType() == ActionType.INVENTORY_CLICK) {
                bw.write("INVENTORY_CLICK " + ((ActionInventoryClick) action).getSlot());
            } else if (action.getType() == ActionType.INVENTORY_CLOSE) {
                bw.write("INVENTORY_CLOSE");
            } else if (action.getType() == ActionType.TELEPORT) {
                ActionTeleport teleport = (ActionTeleport) action;
                if (teleport.getPlayer() != null) {
                    bw.write("TELEPORT " + teleport.getPlayer().getName());
                } else {
                    Location teleportLocation = teleport.getTeleportLocation();
                    bw.write("TELEPORT " + teleportLocation.getBlockX() + " " + teleportLocation.getBlockY() + " " + teleportLocation.getBlockZ());
                }
            } else if (action.getType() == ActionType.MOUNT) {
                bw.write("MOUNT");
            } else if (action.getType() == ActionType.DISMOUNT) {
                bw.write("DISMOUNT");
            } else {
                bw.write("WAIT " + ((ActionWait) action).getDelay());
            }

            bw.newLine();
        }

        bw.close();
    }

    public static Macro loadMacro(String name) throws IOException {
        File macroFile = new File("plugins/FakePlayers/macros/" + name + ".fpmacro");

        List<Action> actions = new ArrayList<>();

        FileInputStream fis = new FileInputStream(macroFile);

        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

        String line = reader.readLine();
        for (int i = 0; line != null; i++) {
            String[] references = line.split(" ");

            ActionType type = ActionType.valueOf(references[0]);

            if (type == ActionType.ATTACK) {
                if (references.length > 1) {
                    throw new RuntimeException("Function ATTACK accepts no arguments. Line: " + i + "(" + macroFile.getPath() + ")");
                } else {
                    actions.add(new ActionAttack());
                }
            } else if (type == ActionType.CHAT) {
                StringBuilder message = new StringBuilder();

                for (int j = 1; j < references.length; j++) {
                    message.append(" ").append(references[j]);
                }

                message = new StringBuilder(message.substring(1));

                actions.add(new ActionChat(message.toString()));
            } else if (type == ActionType.INTERACT) {
                if (references.length > 1) {
                    throw new RuntimeException("Function INTERACT accepts no arguments. Line: " + i + "(" + macroFile.getPath() + ")");
                } else {
                    actions.add(new ActionInteract());
                }
            } else if (type == ActionType.INVENTORY_CLICK) {
                if (references.length != 2) {
                    throw new RuntimeException("Function INVENTORY_CLICK (SLOT) does not accept " + (references.length - 1) + " arguments. Line: " + i + "(" + macroFile.getPath() + ")");
                } else {
                    try {
                        actions.add(new ActionInventoryClick(Integer.parseInt(references[1])));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Function INVENTORY_CLICK (SLOT) accepts INTEGER as its argument. Line: " + i + "(" + macroFile.getPath() + ")");
                    }
                }
            } else if (type == ActionType.INVENTORY_CLOSE) {
                if (references.length > 1) {
                    throw new RuntimeException("Function INVENTORY_CLOSE accepts no arguments. Line: " + i + "(" + macroFile.getPath() + ")");
                } else {
                    actions.add(new ActionInventoryClose());
                }
            } else if (type == ActionType.TELEPORT) {
                if (references.length != 2 && references.length != 4) {
                    throw new RuntimeException("Functions TELEPORT (PLAYER) and TELEPORT (X) (Y) (Z) do not accept " + (references.length - 1) + " arguments." +
                            " Line: " + i + "(" + macroFile.getPath() + ")");
                } else if (references.length == 2) {
                    actions.add(new ActionTeleport(Bukkit.getPlayer(references[1])));
                } else {
                    try {
                        actions.add(new ActionTeleport(new Location(Bukkit.getWorlds().get(0), Integer.parseInt(references[1]), Integer.parseInt(references[2]),
                                Integer.parseInt(references[3]))));
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Function TELEPORT (X) (Y) (Z) accepts INTEGER, INTEGER, INTEGER as its arguments. Line: " + i + "(" + macroFile.getPath() + ")");
                    }
                }
            } else if (type == ActionType.WAIT) {
                if (references.length != 2) {
                    throw new RuntimeException("Function WAIT (TIME) does not accept " + (references.length - 1) + " arguments. Line: " + i + "(" + macroFile.getPath() + ")");
                } else {
                    try {
                        if (references[1].contains("ms")) {
                            actions.add(new ActionWait(Integer.parseInt(references[1].replace("ms", ""))));
                        } else {
                            actions.add(new ActionWait(Integer.parseInt(references[1])));
                        }
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Function WAIT (TIME) accepts INTEGER (Milliseconds) as its argument. Line: " + i + "(" + macroFile.getPath() + ")");
                    }
                }
            } else if (type == ActionType.DISMOUNT) {
                if (references.length > 1) {
                    throw new RuntimeException("Function DISMOUNT accepts no arguments. Line: " + i + "(" + macroFile.getPath() + ")");
                } else {
                    actions.add(new ActionDismount());
                }
            } else {
                throw new RuntimeException("Could not find function '" + references[0] + "'.");
            }

            line = reader.readLine();
        }

        return new Macro(actions, name);
    }
}
