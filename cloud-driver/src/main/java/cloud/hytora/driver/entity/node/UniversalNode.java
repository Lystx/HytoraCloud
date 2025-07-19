package cloud.hytora.driver.entity.node;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.node.base.AbstractNode;
import cloud.hytora.driver.entity.node.data.INodeCycleData;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.packets.other.PacketDriverLogging;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.config.INodeConfig;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityNode;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.*;

@Getter
@NoArgsConstructor
@Setter
public class UniversalNode extends AbstractNode {

    private PacketChannel channel;

    public UniversalNode(INodeConfig config, INodeCycleData lastCycleData) {
        super(config, lastCycleData);
    }

    @Override
    public void sendPacket(IPacket packet) {
        packet.publishTo(this.getName());
    }

    @Override
    public void shutdown() {
        this.sendPacket(PacketCloudEntityNode.forNodeShutdown(this));
    }

    @Override
    public void log(String message, Object... args) {
        this.sendPacket(new PacketDriverLogging(this, StringUtils.formatMessage(message, args)));
    }

    @Override
    public void stopServer(CloudService server) {
        this.sendPacket(PacketCloudEntityNode.forServerStop(server, false));
    }

    @Override
    public Task<NetworkResponseState> stopServerAsync(CloudService server) {

        Task<NetworkResponseState> task = Task.empty();

        CloudDriver.getInstance()
                .getExecutor()
                .getPacketChannel()
               // .overrideExecutor(this)
                .sendQuery()
                .execute(PacketCloudEntityNode.forServerStop(server, true))
                .onTaskSucess(e -> {
                    task.setResult(e.state());
                }).onTaskFailed(task::setFailure);
        return task;
    }

    @Override
    public void startServer(CloudService server) {
        this.sendPacket(PacketCloudEntityNode.forServerStart(server, false));
    }

    @Override
    public Task<NetworkResponseState> startServerAsync(CloudService server) {

        Task<NetworkResponseState> task = Task.empty();
        CloudDriver.getInstance()
                .getExecutor()
                .getPacketChannel()
                //.overrideExecutor(this)
                .sendQuery()
                .execute(PacketCloudEntityNode.forServerStart(server, true))
                .onTaskSucess(e -> {
                    task.setResult(e.state());
                }).onTaskFailed(task::setFailure);

        return task;
    }

}
