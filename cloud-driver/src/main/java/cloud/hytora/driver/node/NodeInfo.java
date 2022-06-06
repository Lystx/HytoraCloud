package cloud.hytora.driver.node;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.networking.EndpointNetworkExecutor;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.networking.packets.RedirectPacket;
import cloud.hytora.driver.networking.packets.node.NodeRequestServerStartPacket;
import cloud.hytora.driver.networking.packets.node.NodeRequestServerStopPacket;
import cloud.hytora.driver.networking.packets.node.NodeRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.node.config.DefaultNodeConfig;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.utils.ServiceState;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class NodeInfo implements Node {

    private String name;
    private ConnectionType type;
    private INodeConfig config;
    private NodeCycleData lastCycleData;

    @Override
    public List<CloudServer> getRunningServers() {
        return CloudDriver.getInstance()
                .getServiceManager()
                .getAllCachedServices()
                .stream()
                .filter(it ->
                        it.getServiceState() == ServiceState.ONLINE &&
                                it.getConfiguration().getNode().equalsIgnoreCase(this.name)
                ).collect(Collectors.toList());
    }

    @Override
    public void sendPacket(Packet packet) {
        if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
            ((EndpointNetworkExecutor)CloudDriver.getInstance().getExecutor()).sendPacket(packet, this);
        } else {
            CloudDriver.getInstance().getExecutor().sendPacket(new RedirectPacket(this.name, packet));
        }
    }


    @Override
    public void shutdown() {
        this.sendPacket(new NodeRequestShutdownPacket(this.name));
    }

    @Override
    public void log(String message, Object... args) {
        this.sendPacket(new DriverLoggingPacket(this, StringUtils.formatMessage(message, args)));
    }

    @Override
    public void stopServer(CloudServer server) {
        this.sendPacket(new NodeRequestServerStopPacket(server.getName()));
    }

    @Override
    public void startServer(CloudServer server) {
        this.sendPacket(new NodeRequestServerStartPacket(server));
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                name = buf.readString();
                type = buf.readEnum(ConnectionType.class);
                config = buf.readObject(DefaultNodeConfig.class);
                lastCycleData = buf.readObject(NodeCycleData.class);
                break;

            case WRITE:
                buf.writeString(name);
                buf.writeEnum(type);
                buf.writeObject(config);
                buf.writeObject(lastCycleData);
                break;
        }
    }

}
