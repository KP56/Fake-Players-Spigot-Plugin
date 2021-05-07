package me.KP56.FakePlayers.Socket;

import me.KP56.FakePlayers.FakePlayer;
import me.KP56.FakePlayers.Main;
import org.bukkit.Bukkit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FakePlayersSocket {
    public static final FakePlayersSocket fakePlayersSocket = new FakePlayersSocket();

    public void start(String host, int port) {
        new Thread(() -> {
            try {
                while (true) {
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket s = serverSocket.accept();
                    if (s.getInetAddress().equals(InetAddress.getByName(host))) {
                        DataInputStream dis = new DataInputStream(s.getInputStream());
                        String str = dis.readUTF();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> receiveMessage(str), 0);
                    } else {
                        Bukkit.getLogger().warning("[FakePlayers] An unregistered BungeeCord tried to connect via FakePlayersSocket. If it's yours, add its ip to config.yml. IP: " +
                                s.getInetAddress().toString());
                    }
                    serverSocket.close();
                }
            } catch (IOException e) {
                Bukkit.getLogger().warning("[FakePlayers] Could not bind to port.");
            }
        }).start();
    }

    private void receiveMessage(String message) {
        String[] arr = message.split(" ");
        if (arr.length == 3) {
            if (arr[0].equals("fakeplayers")) {
                if (arr[1].equals("summon")) {
                    FakePlayer.summon(arr[2]);
                } else if (arr[1].equals("disband")) {
                    FakePlayer.getFakePlayer(arr[2]).removePlayer();
                }
            }
        }
    }

    public void send(String host, int port, String message) {
        new Thread(() -> {
            try {
                Socket clientSocket = new Socket(host, port);
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                dataOutputStream.close();
                clientSocket.close();
            } catch (IOException e) {
                Bukkit.getLogger().warning("[FakePlayers] Could not connect to the server.");
            }
        }).start();
    }
}
