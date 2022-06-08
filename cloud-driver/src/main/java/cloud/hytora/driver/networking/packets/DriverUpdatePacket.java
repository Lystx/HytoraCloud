package cloud.hytora.driver.networking.packets;

import cloud.hytora.driver.networking.PacketSender;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeInfo;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.configuration.DefaultServerConfiguration;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.configuration.bundle.ConfigurationParent;
import cloud.hytora.driver.services.configuration.bundle.DefaultConfigurationParent;
import cloud.hytora.driver.services.impl.SimpleServiceInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class DriverUpdatePacket extends Packet {

    private Collection<ServerConfiguration> groups;
    private Collection<ConfigurationParent> parentGroups;
    private Collection<ServiceInfo> allCachedServices;
    private Collection<CloudPlayer> cloudPlayers;
    private Collection<Node> connectedNodes;


    public static void publishUpdate(PacketSender sender) {
        sender.sendPacket(new DriverUpdatePacket());
    }

    public DriverUpdatePacket() {
        this(
                CloudDriver.getInstance().getConfigurationManager().getAllCachedConfigurations(),
                CloudDriver.getInstance().getConfigurationManager().getAllParentConfigurations(),
                CloudDriver.getInstance().getServiceManager().getAllCachedServices(),
                CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers(),
                CloudDriver.getInstance().getNodeManager().getAllNodes()
        );
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        //do not modify order of reading / writing

        switch (state) {

            case READ:

                parentGroups = buf.readWrapperObjectCollection(DefaultConfigurationParent.class);
                CloudDriver.getInstance().getConfigurationManager().setAllParentConfigurations(parentGroups);

                groups = buf.readWrapperObjectCollection(DefaultServerConfiguration.class);
                CloudDriver.getInstance().getConfigurationManager().setAllCachedConfigurations(groups);

                allCachedServices = buf.readWrapperObjectCollection(SimpleServiceInfo.class);
                CloudDriver.getInstance().getServiceManager().setAllCachedServices((List<ServiceInfo>) allCachedServices);

                cloudPlayers = buf.readWrapperObjectCollection(DefaultCloudPlayer.class);
                CloudDriver.getInstance().getPlayerManager().setAllCachedCloudPlayers((List<CloudPlayer>) cloudPlayers);

                connectedNodes = buf.readWrapperObjectCollection(NodeInfo.class);
                CloudDriver.getInstance().getNodeManager().setAllConnectedNodes((List<Node>) connectedNodes);

                break;

            case WRITE:
                buf.writeObjectCollection(parentGroups);
                buf.writeObjectCollection(groups);
                buf.writeObjectCollection(allCachedServices);
                buf.writeObjectCollection(cloudPlayers);
                buf.writeObjectCollection(connectedNodes);
                break;
        }
    }
}
