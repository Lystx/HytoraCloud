package cloud.hytora.node.remote;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.language.Translation;
import cloud.hytora.driver.networking.Cluster;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.cluster.ClusterExecutor;
import cloud.hytora.driver.networking.cluster.client.AdvancedClusterParticipant;
import cloud.hytora.driver.networking.packets.other.PacketRedirecting;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class RemoteClusterExecutor extends AdvancedClusterParticipant implements Cluster {


    public RemoteClusterExecutor(String authKey, String clientName, Document customData) {
        super(authKey, clientName, ConnectionType.NODE, customData);
    }

    @Override
    public void onAuthenticationChanged(PacketChannel wrapper) {
        CloudDriver.getInstance().getLogger().info(Translation.of("remote.client.authentication.changed"));
    }

    @Override
    public void onActivated(ChannelHandlerContext ctx) {
        CloudDriver.getInstance().getLogger().info(Translation.of("remote.client.connection.established"));
    }

    @Override
    public void onClose(ChannelHandlerContext ctx) {
        CloudDriver.getInstance().getLogger().info(Translation.of("remote.client.connection.closed"));
    }

    @Override
    public <T extends IPacket> void handlePacket(PacketChannel channel, @NotNull T packet) {
        super.handlePacket(channel, packet);
    }

    @Override
    public void sendPacket(IPacket packet, String... receiver) {
        for (String re : receiver) {
            sendPacket(new PacketRedirecting(re, packet));
        }
    }

    @Override
    public void sendPacket(IPacket packet, NetworkComponent component) {
        sendPacket(new PacketRedirecting(component.getName(), packet));
    }

    @Override
    public PacketChannel getConnectedChannel(Channel channel) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.HEADNODE);
    }

    @Override
    public @NotNull Collection<PacketChannel> getConnectedChannels(ConnectionType type) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.HEADNODE);
    }

    @Override
    public PacketChannel getConnectedChannel(String name) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.HEADNODE);
    }

    @Override
    public PacketChannel getConnectedChannel(UUID uniqueId) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.HEADNODE);
    }

    @Override
    public void sendPacketToAll(IPacket packet) {
        packet.publishTo("ALL");
    }


    @Override
    public Task<EndpointNetworkExecutor> openConnection(String hostname, int port) {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.HEADNODE);
    }

    @Override
    public @NotNull Collection<PacketChannel> getAllConnectedChannels() {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.HEADNODE);
    }

    @Override
    public void registerStats(CloudService service) {

    }

    @Override
    public long getStats(CloudService service) {
        return 0;
    }

    @Override
    public String getHostName() {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.HEADNODE);
    }

    @Override
    public int getPort() {
        throw new IncompatibleDriverEnvironmentException(CloudDriver.Environment.HEADNODE);
    }
}
