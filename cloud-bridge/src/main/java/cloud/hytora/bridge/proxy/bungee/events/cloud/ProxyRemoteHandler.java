package cloud.hytora.bridge.proxy.bungee.events.cloud;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheRegisterEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;

import cloud.hytora.driver.networking.packets.player.CloudPlayerComponentMessagePacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerKickPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerPlainMessagePacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerSendServicePacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.remote.Remote;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import net.md_5.bungee.chat.ComponentSerializer;

import java.net.InetSocketAddress;

public class ProxyRemoteHandler {

    public ProxyRemoteHandler() {

        //register fallback
        this.registerService("fallback", new InetSocketAddress("127.0.0.1", 0));

        //load all current groups
        for (CloudServer allCachedService : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            ServerConfiguration serviceGroup = allCachedService.getConfiguration();
            if (!serviceGroup.getVersion().isProxy()) {
                registerService(allCachedService);
            }
        }

        //register events
        CloudDriver.getInstance().getEventManager().registerListener(this);

        //register network handler
        Remote.getInstance().getExecutor().registerPacketHandler(new KickHandler());
        Remote.getInstance().getExecutor().registerPacketHandler(new SendHandler());
        Remote.getInstance().getExecutor().registerPacketHandler(new MessageHandler());
        Remote.getInstance().getExecutor().registerPacketHandler(new ComponentHandler());
    }


    @EventListener
    public void handle(CloudServerCacheRegisterEvent event) {
        CloudServer cloudServer = event.getServer();
        if (!cloudServer.getConfiguration().getVersion().isProxy()) {
            this.registerService(cloudServer);
        }
    }

    @EventListener
    public void handle(CloudServerCacheUnregisterEvent event) {
        ProxyServer.getInstance().getServers().remove(event.getService());
    }

    private void registerService(String name, InetSocketAddress socketAddress) {
        ProxyServer.getInstance().getServers().put(name, ProxyServer.getInstance().constructServerInfo(name, socketAddress, "CloudServer", false));
    }


    private void registerService(CloudServer service) {
        this.registerService(service.getName(), new InetSocketAddress(service.getHostName(), service.getPort()));
    }

    private static class KickHandler implements PacketHandler<CloudPlayerKickPacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerKickPacket packet) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
            if (player == null) {
                return;
            }
            player.disconnect(new TextComponent(packet.getReason()));
        }
    }

    private static class MessageHandler implements PacketHandler<CloudPlayerPlainMessagePacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerPlainMessagePacket packet) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
            if (player == null) {
                return;
            }
            player.disconnect(new TextComponent(packet.getMessage()));
        }
    }

    private static class ComponentHandler implements PacketHandler<CloudPlayerComponentMessagePacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerComponentMessagePacket packet) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
            if (player == null) {
                return;
            }
            // TODO: 22.05.2022 transform component to bungee component
            ChatComponent message = packet.getMessage();

            player.sendMessage(ComponentSerializer.parse(message.toString()));
        }
    }

    private static class SendHandler implements PacketHandler<CloudPlayerSendServicePacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerSendServicePacket packet) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
            if (player == null || player.getServer().getInfo().getName().equals(packet.getService())) {
                return;
            }
            player.connect(ProxyServer.getInstance().getServerInfo(packet.getService()));
        }
    }

}
