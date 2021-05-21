package me.KP56.FakePlayers.BungeeCord;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import me.KP56.FakePlayers.BungeeCord.Listeners.CommandListener;
import me.KP56.FakePlayers.BungeeCord.Listeners.ServerConnectListener;
import me.KP56.FakePlayers.BungeeCord.Socket.FakePlayersSocket;
import me.KP56.FakePlayers.BungeeCord.Socket.FakeSocketChannel;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.LegacyDecoder;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.MinecraftEncoder;
import net.md_5.bungee.protocol.Protocol;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Plugin {

    private static Main main;
    public Configuration configuration;
    private List<UserConnection> fakePlayers = new ArrayList<>();

    public static Main getMain() {
        return main;
    }

    @Override
    public void onEnable() {
        main = this;

        getProxy().getPluginManager().registerListener(main, new CommandListener());
        getProxy().getPluginManager().registerListener(main, new ServerConnectListener());

        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<ServerInfo> servers = new ArrayList<>(BungeeCord.getInstance().getServers().values());

        FakePlayersSocket.fakePlayersSocket.start(servers, configuration.getInt("bungee-fakeplayers-port"));
    }

    public void addFakePlayer(String name, ServerConnection serverConnection) {
        NioSocketChannel channel = new NioSocketChannel(new NioServerSocketChannel(), new FakeSocketChannel(SelectorProvider.provider()));

        ListenerInfo listener = (ListenerInfo) BungeeCord.getInstance().config.getListeners().toArray()[0];

        FakeChannelHandlerContext fakeChannelHandlerContext = new FakeChannelHandlerContext(channel);

        ChannelWrapper channelWrapper = new ChannelWrapper(fakeChannelHandlerContext);

        InitialHandler initialHandler = new InitialHandler(BungeeCord.getInstance(), listener);

        try {
            initialHandler.connected(channelWrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Field uniqueId = InitialHandler.class.getDeclaredField("uniqueId");
            uniqueId.setAccessible(true);
            uniqueId.set(initialHandler, UUID.randomUUID());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        UserConnection fakeConnection = new UserConnection(BungeeCord.getInstance(), channelWrapper, name,
                initialHandler);

        PreLoginEvent preLoginEvent = new PreLoginEvent(initialHandler, (preLoginEvent1, throwable) -> {});

        preLoginEvent.setCancelReason(new BaseComponent() {
            @Override
            public BaseComponent duplicate() {
                return null;
            }
        });

        fakePlayers.add(fakeConnection);

        try {
            BungeeCord.getInstance().getPluginManager().callEvent(preLoginEvent);
            BungeeCord.getInstance().getPluginManager().callEvent(new LoginEvent(initialHandler, (preLoginEvent1, throwable) -> {
            }));
        } catch (Exception ignored) {

        }

        try {
            initialHandler.connected(channelWrapper);
            PipelineUtils.BASE.initChannel(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.pipeline().addBefore("frame-decoder", "legacy-decoder", new LegacyDecoder());
        channel.pipeline().addAfter("frame-decoder", "packet-decoder", new MinecraftDecoder(Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion()));
        channel.pipeline().addAfter("frame-prepender", "packet-encoder", new MinecraftEncoder(Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion()));
        channel.pipeline().get(HandlerBoss.class).setHandler(initialHandler);

        BungeeCord.getInstance().addConnection(fakeConnection);

        ServerConnectEvent serverConnectEvent = new ServerConnectEvent(fakeConnection, serverConnection.getInfo());
        BungeeCord.getInstance().getPluginManager().callEvent(serverConnectEvent);

        fakeConnection.setServer(serverConnection);
        ((BungeeServerInfo) serverConnectEvent.getTarget()).addPlayer(fakeConnection);

        PostLoginEvent postLoginEvent = new PostLoginEvent(fakeConnection);

        try {
            BungeeCord.getInstance().getPluginManager().callEvent(postLoginEvent);
        } catch (Exception ignored) {

        }
    }


    private boolean checkOnline(ServerInfo info) {

        AtomicBoolean returnValue = new AtomicBoolean(false);

        info.ping((serverPing, throwable) -> {
            if (throwable != null) {
                returnValue.set(false);
            } else {
                returnValue.set(true);
            }
        });

        return returnValue.get();
    }

    private void pingScheduler() {
        BungeeCord.getInstance().getScheduler().schedule(this, () -> {
            Map<String, ServerInfo> servers = BungeeCord.getInstance().getServers();

            for (Map.Entry<String, ServerInfo> entry : servers.entrySet()) {
                if (!checkOnline(entry.getValue())) {
                    for (UserConnection user : fakePlayers) {
                        if (user.getServer().getInfo().getName().equals(entry.getKey())) {
                            ServerInfo def = BungeeCord.getInstance().getServers().get(user.getPendingConnection().getListener().getDefaultServer());

                            int port = Main.getMain().configuration.getInt("servers." + def.getName() + ".fakeplayers-port");

                            summon(user.getName(), def.getAddress().toString(), port);
                        }
                    }
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    public boolean isFakePlayer(String fakePlayer) {
        for (UserConnection connection : fakePlayers) {
            if (fakePlayer.equals(connection.getName())) {
                return true;
            }
        }

        return false;
    }

    public void removeFakePlayer(String fakePlayer) {
        List<UserConnection> copy = new ArrayList<>(fakePlayers);
        for (UserConnection connection : copy) {
            if (fakePlayer.equals(connection.getName())) {
                BungeeCord.getInstance().removeConnection(connection);
                connection.getServer().getInfo().removePlayer(connection);
                fakePlayers.remove(connection);
            }
        }
    }

    public void removeAll(ServerConnection serverConnection) {
        List<UserConnection> copy = new ArrayList<>(fakePlayers);
        for (UserConnection connection : copy) {
            if (connection.getServer().getInfo().getName().equals(serverConnection.getInfo().getName())) {
                BungeeCord.getInstance().removeConnection(connection);
                fakePlayers.remove(connection);
            }
        }
    }

    public String getIP(String address) {
        String[] split = address.split("/");
        String ipWithPort;
        if (split.length == 2) {
            ipWithPort = split[1];
        } else {
            ipWithPort = split[0];
        }

        String[] split2 = ipWithPort.split(":");

        return split2[0];
    }

    public void summon(String name, String address, int port) {
        String ip = getIP(address);

        try {
            FakePlayersSocket.fakePlayersSocket.send(ip, port, "fakeplayers summon " + name);
        } catch (IOException e) {
            BungeeCord.getInstance().getLogger().warning("Could not connect to the server: " + ip + ":" + port + ".");
        }
    }

    public void disband(String name, String address, int port) {
        String ip = getIP(address);

        try {
            FakePlayersSocket.fakePlayersSocket.send(ip, port, "fakeplayers disband " + name);
        } catch (IOException e) {
            BungeeCord.getInstance().getLogger().warning("Could not connect to the server: " + ip + ":" + port + ".");
        }
    }

    public UserConnection getFakePlayer(String name) {
        for (UserConnection connection : fakePlayers) {
            if (connection.getName().equals(name)) {
                return connection;
            }
        }
        return null;
    }
}
