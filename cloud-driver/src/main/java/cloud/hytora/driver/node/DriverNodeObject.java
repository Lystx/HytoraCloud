package cloud.hytora.driver.node;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.networking.packets.node.NodeRequestServerStartPacket;
import cloud.hytora.driver.networking.packets.node.NodeRequestServerStopPacket;
import cloud.hytora.driver.networking.packets.node.NodeRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.node.base.AbstractNode;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.data.INodeData;
import cloud.hytora.driver.services.ICloudServer;
import lombok.*;

@Getter
@NoArgsConstructor
@Setter
public class DriverNodeObject extends AbstractNode {

    public DriverNodeObject(INodeConfig config, INodeData lastCycleData) {
        super(config, lastCycleData);
    }

    @Override
    public void sendPacket(IPacket packet) {
        packet.publishTo(this.getName());
    }


    @Override
    public void shutdown() {
        this.sendPacket(new NodeRequestShutdownPacket(this.getName()));
    }

    @Override
    public void log(String message, Object... args) {
        this.sendPacket(new DriverLoggingPacket(this, StringUtils.formatMessage(message, args)));
    }

    @Override
    public void stopServer(ICloudServer server) {
        this.sendPacket(new NodeRequestServerStopPacket(server.getName()));
    }

    @Override
    public Task<NetworkResponseState> stopServerAsync(ICloudServer server) {
        return Task.empty(); // TODO: 01.08.2022
    }

    @Override
    public void startServer(ICloudServer server) {
        this.sendPacket(new NodeRequestServerStartPacket(server));
    }

    @Override
    public Task<NetworkResponseState> startServerAsync(ICloudServer server) {
        return Task.empty(); // TODO: 01.08.2022
    }

}
