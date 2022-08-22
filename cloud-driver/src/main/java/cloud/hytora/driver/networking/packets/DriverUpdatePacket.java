package cloud.hytora.driver.networking.packets;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.driver.DriverCacheUpdateEvent;
import cloud.hytora.driver.networking.IPacketExecutor;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.node.INodeManager;
import cloud.hytora.driver.node.UniversalNode;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.task.UniversalServiceTask;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.task.bundle.DefaultTaskGroup;
import cloud.hytora.driver.services.impl.UniversalCloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class DriverUpdatePacket extends AbstractPacket {

    private Collection<IServiceTask> serviceTasks;
    private Collection<TaskGroup> parentGroups;
    private Collection<ICloudServer> allCachedServices;
    private Collection<ICloudPlayer> cloudPlayers;
    private Collection<INode> connectedNodes;


    public static void publishUpdate(IPacketExecutor sender) {
        sender.sendPacket(new DriverUpdatePacket());

        Logger.constantInstance().debug("Published Update using {}", sender);
    }

    public DriverUpdatePacket() {
        this(
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getAllCachedTasks(),
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getAllTaskGroups(),
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllCachedServices(),
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getAllCachedCloudPlayers(),
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).getAllCachedNodes()
        );
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        //do not modify order of reading / writing

        switch (state) {

            case READ:

                parentGroups = buf.readWrapperObjectCollection(DefaultTaskGroup.class);
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).setAllTaskGroups(parentGroups);

                serviceTasks = buf.readWrapperObjectCollection(UniversalServiceTask.class);
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).setAllCachedTasks(serviceTasks);

                allCachedServices = buf.readWrapperObjectCollection(UniversalCloudServer.class);
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).setAllCachedServices((List<ICloudServer>) allCachedServices);

                cloudPlayers = buf.readWrapperObjectCollection(UniversalCloudPlayer.class);
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).setAllCachedCloudPlayers((List<ICloudPlayer>) cloudPlayers);

                connectedNodes = buf.readWrapperObjectCollection(UniversalNode.class);
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(INodeManager.class).setAllCachedNodes((List<INode>) connectedNodes);

                CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyLocally(new DriverCacheUpdateEvent());
                break;

            case WRITE:
                buf.writeObjectCollection(parentGroups);
                buf.writeObjectCollection(serviceTasks);
                buf.writeObjectCollection(allCachedServices);
                buf.writeObjectCollection(cloudPlayers);
                buf.writeObjectCollection(connectedNodes);
                break;
        }
    }
}
