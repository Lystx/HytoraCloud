package cloud.hytora.driver.node;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.node.packet.NodeRequestServerStartPacket;
import cloud.hytora.driver.node.packet.NodeRequestServerStopPacket;
import cloud.hytora.driver.node.packet.NodeRequestShutdownPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferedResponse;
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
public class UniversalNode extends AbstractNode {

    public UniversalNode(INodeConfig config, INodeData lastCycleData) {
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
        this.sendPacket(new NodeRequestServerStopPacket(server.getName(), false));
    }

    @Override
    public Task<NetworkResponseState> stopServerAsync(ICloudServer server) {
        return new NodeRequestServerStartPacket(server, true)
                .awaitResponse(this.getName())
                .registerListener((ExceptionallyConsumer<Task<BufferedResponse>>) task -> {
                    if (!task.isPresent()) {
                        task.setFailure(task.error());
                    }
                }).map(BufferedResponse::state);
    }

    @Override
    public void startServer(ICloudServer server) {
        this.sendPacket(new NodeRequestServerStartPacket(server, false));
    }

    @Override
    public Task<NetworkResponseState> startServerAsync(ICloudServer server) {
        return new NodeRequestServerStopPacket(server.getName(), true)
                .awaitResponse(this.getName())
                .registerListener((ExceptionallyConsumer<Task<BufferedResponse>>) task -> {
                    if (!task.isPresent()) {
                        task.setFailure(task.error());
                    }
                }).map(BufferedResponse::state);
    }

}
