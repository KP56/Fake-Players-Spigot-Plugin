package me.KP56.FakePlayers.BungeeCord.Socket;

import me.KP56.FakePlayers.BungeeCord.Main;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ChatEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class FakePlayersSocket {
    public static final FakePlayersSocket fakePlayersSocket = new FakePlayersSocket();

    public void start(List<ServerInfo> hosts, int port) {
        new Thread(() -> {
            try {
                while (true) {
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket s = serverSocket.accept();

                    for (ServerInfo host : hosts) {
                        if (s.getInetAddress().equals(host.getAddress().getAddress())) {
                            DataInputStream dis = new DataInputStream(s.getInputStream());
                            String str = dis.readUTF();
                            receiveMessage(str);
                            break;
                        }
                    }
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void receiveMessage(String message) {
        String[] arr = message.split(" ");

        if (arr[0].equals("chat")) {
            UserConnection fakePlayer = Main.getMain().getFakePlayer(arr[1]);
            StringBuilder msg = new StringBuilder();

            for (int i = 2; i < arr.length; i++) {
                msg.append(arr[i]);
            }

            BungeeCord.getInstance().getPluginManager().callEvent(new ChatEvent(fakePlayer, fakePlayer.getServer(), msg.toString()));
        }
    }

    public void send(String host, int port, String message) throws IOException {
        Socket clientSocket = new Socket(host, port);
        DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();
        dataOutputStream.close();
        clientSocket.close();
    }
}
