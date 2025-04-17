package cloud.hytora.bridge.proxy.vera.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.component.SimpleComponent;
import cloud.hytora.driver.component.event.ComponentEvent;
import cloud.hytora.driver.component.event.click.ClickEvent;
import cloud.hytora.driver.component.event.hover.HoverAction;
import cloud.hytora.driver.component.event.hover.HoverEvent;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.driver.DriverCacheUpdateEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.task.TaskMaintenanceChangeEvent;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.player.packet.*;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.remote.Remote;
import de.verasoftware.proxy.api.VeraProxy;
import de.verasoftware.proxy.api.component.ChatComponent;
import de.verasoftware.proxy.api.environment.entity.player.Player;
import de.verasoftware.proxy.api.network.NetworkAddress;
import de.verasoftware.proxy.api.server.ProxyServer;

import java.net.InetSocketAddress;
import java.util.List;

public class VeraProxyRemoteHandler {

    public VeraProxyRemoteHandler() {

        //load all current groups
        for (ICloudService allCachedService : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            IServiceTask serviceGroup = allCachedService.getTask();
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
        IServiceTask task = event.getTask();
        CloudMessages cloudMessages = CloudDriver.getInstance().getStorage().get("cloud::messages").toInstance(CloudMessages.class);

        ICloudService thisService = CloudDriver.getInstance().getServiceManager().thisServiceOrNull();

        if (event.isNewMaintenanceValue() && task.getName().equalsIgnoreCase(thisService.getTask().getName())) {

            List<String> whitelistedPlayers = CloudDriver.getInstance().getStorage().getBundle("cloud::whitelist").toInstances(String.class);
            for (ICloudPlayer cp : thisService.getOnlinePlayers()) {
                if (whitelistedPlayers.contains(cp.getName())) {
                    PlayerExecutor.forPlayer(cp).sendMessage(cloudMessages.getMaintenanceKickByPassedMessage());
                    continue;
                }
                PlayerExecutor.forPlayer(cp).disconnect(cloudMessages.getNetworkCurrentlyInMaintenance());
            }

        }
    }


    @EventListener
    public void handle(ServiceRegisterEvent event) {
        ICloudService cloudServer = event.getCloudServer();
        if (cloudServer.getTask() == null) {
            return;
        }
        if (!cloudServer.getTask().getVersion().isProxy()) {
            this.registerService(cloudServer);
        }
    }

    @EventListener
    public void handle(ServiceUnregisterEvent event) {
        ProxyServer registeredServer = VeraProxy.getInstance().getRegisteredServer(event.getService());
        if (registeredServer != null) {

            VeraProxy.getInstance().getRegisteredServers().remove(registeredServer);
        }
    }

    @EventListener
    public void handle(DriverCacheUpdateEvent event) {
        VeraProxy.getInstance().getRegisteredServers().clear();
        for (ICloudService service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            if (!service.getTask().getVersion().isProxy()) {
                this.registerService(service);
            }
        }
    }
    private void registerService(String name, InetSocketAddress socketAddress) {
        if (VeraProxy.getInstance().getRegisteredServer(name) != null) {
            return;
        }
        VeraProxy.getInstance().registerServer(
                VeraProxy
                        .getInstance()
                        .createServer(name, NetworkAddress.parse(socketAddress.toString()), "CloudServer")
        );
    }


    private void registerService(ICloudService service) {
        this.registerService(service.getName(), new InetSocketAddress(service.getHostName(), service.getPort()));
    }

    private static class TabHandler implements PacketHandler<CloudPlayerTabListPacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerTabListPacket packet) {
            Player player = VeraProxy.getInstance().getOnlinePlayer(packet.getUuid());
            if (player == null) {
                return;
            }

            String header = packet.getHeader();
            String footer = packet.getFooter();

            player.setTabHeaderAndFooter(
                    ChatComponent.text(header),
                    ChatComponent.text(footer)
            );
        }
    }

    private static class KickHandler implements PacketHandler<CloudPlayerKickPacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerKickPacket packet) {
            Player player = VeraProxy.getInstance().getOnlinePlayer(packet.getUuid());
            if (player == null) {
                return;
            }
            player.disconnect(ChatComponent.text(packet.getReason()));
        }
    }

    private static class MessageHandler implements PacketHandler<CloudPlayerPlainMessagePacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerPlainMessagePacket packet) {
            Player player = VeraProxy.getInstance().getOnlinePlayer(packet.getUuid());
            if (player == null) {
                return;
            }
            player.sendMessage(packet.getMessage());
        }
    }


    /**
     * Creates a  from a {@link SimpleComponent}
     *
     * @param chatComponent the cloudComponent
     * @return built md5 textComponent
     */
    private ChatComponent createTextComponentFromCloudRecursive(SimpleComponent chatComponent) {
        ChatComponent textComponent = ChatComponent.text(chatComponent.getContent());
        ComponentEvent<ClickEvent> clickEvent = chatComponent.getClickEvent();
        if (clickEvent != null) {
            de.verasoftware.proxy.api.component.event.click.ClickAction clickAction = de.verasoftware.proxy.api.component.event.click.ClickAction.valueOf(clickEvent.getType().name());
            textComponent.setClickEvent(de.verasoftware.proxy.api.component.event.click.ClickEvent.of(clickAction, clickEvent.getValue()));
        }
        ComponentEvent<HoverEvent> hoverEvent = chatComponent.getHoverEvent();
        if (hoverEvent != null) {
            if (hoverEvent.copy().getType() == HoverAction.SHOW_TEXT) {

                textComponent.setHoverEvent(de.verasoftware.proxy.api.component.event.hover.HoverEvent.text(hoverEvent.getValue()));
            } else {
                // TODO: 09.04.2025
            }
        }
        for (Component cloudComponent : chatComponent.getSubComponents()) {
            textComponent.addWith(createTextComponentFromCloudRecursive((SimpleComponent) cloudComponent));
        }

        textComponent.setBold(chatComponent.isBold());
        textComponent.setItalic(chatComponent.isItalic());
        textComponent.setStrikethrough(chatComponent.isStrikeThrough());
        textComponent.setObfuscated(chatComponent.isObfuscated());
        textComponent.setUnderlined(chatComponent.isUnderlined());

        return textComponent;
    }

    private class ComponentHandler implements PacketHandler<CloudPlayerComponentMessagePacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerComponentMessagePacket packet) {
            Player player = VeraProxy.getInstance().getOnlinePlayer(packet.getUuid());
            if (player == null) {
                return;
            }
            Component message = packet.getMessage();

            player.sendMessage(VeraProxyRemoteHandler.this.createTextComponentFromCloudRecursive((SimpleComponent) message));
        }
    }

    private static class SendHandler implements PacketHandler<CloudPlayerSendServicePacket> {

        @Override
        public void handle(PacketChannel wrapper, CloudPlayerSendServicePacket packet) {
            Player player = VeraProxy.getInstance().getOnlinePlayer(packet.getUuid());
            if (player == null || player.getConnectedServer().getName().equals(packet.getService())) {
                return;
            }
            player.connect(VeraProxy.getInstance().getRegisteredServer(packet.getService()));
        }
    }

}
