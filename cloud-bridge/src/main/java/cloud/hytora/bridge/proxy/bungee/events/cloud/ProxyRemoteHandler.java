package cloud.hytora.bridge.proxy.bungee.events.cloud;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.driver.DriverCacheUpdateEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;

import cloud.hytora.driver.event.defaults.task.TaskMaintenanceChangeEvent;
import cloud.hytora.driver.networking.packets.player.*;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.remote.Remote;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import net.md_5.bungee.chat.ComponentSerializer;

import java.net.InetSocketAddress;
import java.util.List;

public class ProxyRemoteHandler {

    public ProxyRemoteHandler() {

        //load all current groups
        for (ServiceInfo allCachedService : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            ServiceTask serviceGroup = allCachedService.getTask();
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
        Remote.getInstance().getExecutor().registerPacketHandler(new TabHandler());
    }

    @EventListener
    public void handle(TaskMaintenanceChangeEvent event) {
        ServiceTask task = event.getTask();
        ServiceInfo thisService = CloudDriver.getInstance().getServiceManager().thisServiceOrNull();

        if (event.isNewMaintenanceValue() && task.getName().equalsIgnoreCase(thisService.getTask().getName())) {

            List<String> whitelistedPlayers = CloudDriver.getInstance().getStorage().getBundle("cloud::whitelist").toInstances(String.class);
            for (CloudPlayer cp : thisService.getOnlinePlayers()) {
                if (whitelistedPlayers.contains(cp.getName())) {
                    PlayerExecutor.forPlayer(cp).sendMessage("§7Maintenance for your Proxy was enabled but you didn't get kicked because you are whitelisted!");
                    continue;
                }
                PlayerExecutor.forPlayer(cp).disconnect("§cThe network is currently in maintenance!"); // TODO: 25.07.2022 custom message
            }

        }
    }


    @EventListener
    public void handle(ServiceRegisterEvent event) {
        ServiceInfo serviceInfo = event.getServiceInfo();
        if (!serviceInfo.getTask().getVersion().isProxy()) {
            this.registerService(serviceInfo);
        }
    }

    @EventListener
    public void handle(ServiceUnregisterEvent event) {
        ProxyServer.getInstance().getServers().remove(event.getService());
    }

    @EventListener
    public void handle(DriverCacheUpdateEvent event) {
        ProxyServer.getInstance().getServers().clear();
        for (ServiceInfo service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (!service.getTask().getVersion().isProxy()) {
                this.registerService(service);
            }
        }
    }
    private void registerService(String name, InetSocketAddress socketAddress) {
        if (ProxyServer.getInstance().getServerInfo(name) != null) {
            return;
        }
        ProxyServer.getInstance().getServers().put(name, ProxyServer.getInstance().constructServerInfo(name, socketAddress, "CloudServer", false));
    }


    private void registerService(ServiceInfo service) {
        this.registerService(service.getName(), new InetSocketAddress(service.getHostName(), service.getPort()));
    }

    private static class TabHandler implements PacketHandler<CloudPlayerTabListPacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerTabListPacket packet) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
            if (player == null) {
                return;
            }

            String header = packet.getHeader();
            String footer = packet.getFooter();

            player.setTabHeader(
                    new TextComponent(header),
                    new TextComponent(footer)
            );
        }
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
            player.sendMessage(packet.getMessage());
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
