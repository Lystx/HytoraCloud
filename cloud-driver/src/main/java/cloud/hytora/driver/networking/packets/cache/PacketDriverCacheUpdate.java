package cloud.hytora.driver.networking.packets.cache;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.command.DriverCommandInfo;
import cloud.hytora.driver.event.defaults.driver.CloudEventDriverCacheUpdate;
import cloud.hytora.driver.networking.PacketSender;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.node.UniversalNode;
import cloud.hytora.driver.entity.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.task.UniversalServiceTask;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.entity.services.task.bundle.DefaultTaskGroup;
import cloud.hytora.driver.entity.services.impl.UniversalCloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class PacketDriverCacheUpdate extends AbstractPacket {

    private Collection<ServiceTask> allCachedServiceTasks;
    private Collection<TaskGroup> allCachedTaskGroups;
    private Collection<CloudService> allCachedServices;
    private Collection<CloudPlayer> allCachedCloudPlayers;
    private Collection<INode> allCachedNodes;
    private Collection<DriverCommandInfo> allRegisteredCommands;


    public static void publishUpdate(PacketSender sender) {
        if (!CloudDriver.getInstance().getNodeManager().isHeadNode()) {
            CloudDriver.getInstance().getLogger().warn("Tried sending {} but this Node is not the HeadNode.", PacketDriverCacheUpdate.class.getName());
            return;
        }
        sender.sendPacket(new PacketDriverCacheUpdate());

        Logger.constantInstance().debug("Published Update using {}", sender);
    }

    public PacketDriverCacheUpdate() {
        this(
                CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks(),
                CloudDriver.getInstance().getServiceTaskManager().getAllCachedTaskGroups(),
                CloudDriver.getInstance().getServiceManager().getAllCachedServices(),
                CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers(),
                CloudDriver.getInstance().getNodeManager().getAllCachedNodes(),
                CloudDriver.getInstance().getCommandManager().getRegisteredCommands()
        );
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        //do not modify order of reading / writing

        switch (state) {

            case READ:

                allCachedTaskGroups = buf.readWrapperObjectCollection(DefaultTaskGroup.class);
                CloudDriver.getInstance().getServiceTaskManager().setAllCachedTaskGroups(allCachedTaskGroups);

                allCachedServiceTasks = buf.readWrapperObjectCollection(UniversalServiceTask.class);
                CloudDriver.getInstance().getServiceTaskManager().setAllCachedTasks(allCachedServiceTasks);

                allCachedServices = buf.readWrapperObjectCollection(UniversalCloudServer.class);
                CloudDriver.getInstance().getServiceManager().setAllCachedServices((List<CloudService>) allCachedServices);

                allCachedCloudPlayers = buf.readWrapperObjectCollection(UniversalCloudPlayer.class);
                CloudDriver.getInstance().getPlayerManager().setCachedCloudPlayers(allCachedCloudPlayers);

                allCachedNodes = buf.readWrapperObjectCollection(UniversalNode.class);
                CloudDriver.getInstance().getNodeManager().setAllCachedNodes((List<INode>) allCachedNodes);

                allRegisteredCommands = buf.readWrapperObjectCollection(DriverCommandInfo.class);
                if (CloudDriver.getInstance().getEnvironment() != CloudDriver.Environment.NODE) {
                    CloudDriver.getInstance().getCommandManager().setRegisteredCommands(allRegisteredCommands);
                }

                CloudDriver.getInstance().getEventManager().callEvent(new CloudEventDriverCacheUpdate());
                break;

            case WRITE:
                buf.writeObjectCollection(allCachedTaskGroups);
                buf.writeObjectCollection(allCachedServiceTasks);
                buf.writeObjectCollection(allCachedServices);
                buf.writeObjectCollection(allCachedCloudPlayers);
                buf.writeObjectCollection(allCachedNodes);
                buf.writeObjectCollection(allRegisteredCommands);
                break;
        }
    }
}
