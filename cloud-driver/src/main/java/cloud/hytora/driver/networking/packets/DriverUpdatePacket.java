package cloud.hytora.driver.networking.packets;

import cloud.hytora.driver.event.defaults.driver.DriverCacheUpdateEvent;
import cloud.hytora.driver.networking.PacketSender;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.node.NodeInfo;
import cloud.hytora.driver.player.impl.DefaultCloudPlayer;
import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.task.DefaultServiceTask;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.task.bundle.DefaultTaskGroup;
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

    private Collection<ServiceTask> groups;
    private Collection<TaskGroup> parentGroups;
    private Collection<ServiceInfo> allCachedServices;
    private Collection<CloudPlayer> cloudPlayers;
    private Collection<Node> connectedNodes;


    public static void publishUpdate(PacketSender sender) {
        sender.sendPacket(new DriverUpdatePacket());
    }

    public DriverUpdatePacket() {
        this(
                CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks(),
                CloudDriver.getInstance().getServiceTaskManager().getAllTaskGroups(),
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

                parentGroups = buf.readWrapperObjectCollection(DefaultTaskGroup.class);
                CloudDriver.getInstance().getServiceTaskManager().setAllTaskGroups(parentGroups);

                groups = buf.readWrapperObjectCollection(DefaultServiceTask.class);
                CloudDriver.getInstance().getServiceTaskManager().setAllCachedTasks(groups);

                allCachedServices = buf.readWrapperObjectCollection(SimpleServiceInfo.class);
                CloudDriver.getInstance().getServiceManager().setAllCachedServices((List<ServiceInfo>) allCachedServices);

                cloudPlayers = buf.readWrapperObjectCollection(DefaultCloudPlayer.class);
                CloudDriver.getInstance().getPlayerManager().setAllCachedCloudPlayers((List<CloudPlayer>) cloudPlayers);

                connectedNodes = buf.readWrapperObjectCollection(NodeInfo.class);
                CloudDriver.getInstance().getNodeManager().setAllConnectedNodes((List<Node>) connectedNodes);

                CloudDriver.getInstance().getEventManager().callEventOnlyLocally(new DriverCacheUpdateEvent());
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
