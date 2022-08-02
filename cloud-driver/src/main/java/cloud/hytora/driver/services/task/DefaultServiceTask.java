package cloud.hytora.driver.services.task;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.event.defaults.task.TaskMaintenanceChangeEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.property.ProtocolPropertyObject;
import cloud.hytora.driver.services.ConfigurableService;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.impl.DefaultConfigurableService;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.fallback.SimpleFallback;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.def.CloudTemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultServiceTask extends ProtocolPropertyObject implements IServiceTask, IBufferObject {

    private String name, parent;
    private Collection<String>possibleNodes;
    private String motd, permission;
    private int memory, defaultMaxPlayers, minOnlineService, maxOnlineService, startOrder;
    private boolean maintenance;

    private int javaVersion;

    private SimpleFallback fallback = new SimpleFallback();
    private ServiceVersion version;
    private Collection<CloudTemplate> templates = new ArrayList<>();

    public void setTemplates(Collection<ServiceTemplate> templates) {
        this.templates = templates.stream().map(t -> ((CloudTemplate)t)).collect(Collectors.toList());
    }

    public Collection<ServiceTemplate> getTemplates() {
        return new ArrayList<>(templates);
    }

    @Override
    public INode findAnyNode() {
        return this.possibleNodes.isEmpty() ? null : CloudDriver.getInstance().getNodeManager().getNodeByNameOrNull(this.possibleNodes.stream().findAny().get());
    }

    @Override
    public Task<INode> findAnyNodeAsync() {
        Task<INode> task = Task.empty();

        Task.runAsync(() -> {
            INode anyNode = findAnyNode();
            if (anyNode == null) {
                task.setFailure(new NullPointerException("No connected Node found!"));
            } else {
                task.setResult(anyNode);
            }
        });

        return task;
    }

    @Override
    public Collection<INode> findPossibleNodes() {
        return null;
    }

    @Override
    public void setNode(@NotNull String... node) {
        this.possibleNodes = new ArrayList<>(Arrays.asList(node));
    }

    @Override
    public ConfigurableService configureFutureService() {
        return new DefaultConfigurableService(this);
    }

    public TaskGroup getTaskGroup() {
        return CloudDriver
                .getInstance()
                .getServiceTaskManager()
                .getTaskGroupByNameOrNull(
                        this.parent);
    }

    @Override
    public List<ICloudServer> getOnlineServices() {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByTask(this);
    }

    @Override
    public void update() {
        CloudDriver.getInstance().getServiceTaskManager().update(this);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        super.applyBuffer(state, buf);

        switch (state) {

            case READ:
                this.name = buf.readString();
                this.parent = buf.readString();
                this.permission = buf.readOptionalString();
                this.possibleNodes = buf.readStringCollection();
                this.motd = buf.readString();

                this.memory = buf.readInt();
                this.defaultMaxPlayers = buf.readInt();
                this.minOnlineService = buf.readInt();
                this.maxOnlineService = buf.readInt();
                this.startOrder = buf.readInt();
                this.javaVersion = buf.readInt();

                this.fallback = buf.readObject(SimpleFallback.class);
                this.maintenance = buf.readBoolean();

                this.version = buf.readEnum(ServiceVersion.class);
                this.templates = buf.readObjectCollection(CloudTemplate.class);
                break;

            case WRITE:
                buf.writeString(this.getName());
                buf.writeString(this.parent);
                buf.writeOptionalString(this.getPermission());
                buf.writeStringCollection(this.getPossibleNodes());
                buf.writeString(this.getMotd());

                buf.writeInt(this.getMemory());
                buf.writeInt(this.getDefaultMaxPlayers());
                buf.writeInt(this.getMinOnlineService());
                buf.writeInt(this.getMaxOnlineService());
                buf.writeInt(this.getStartOrder());
                buf.writeInt(this.getJavaVersion());

                buf.writeObject(this.getFallback());
                buf.writeBoolean(this.isMaintenance());

                buf.writeEnum(this.getVersion());
                buf.writeObjectCollection(this.templates);
                break;
        }
    }

    @Override
    public void setMaintenance(boolean maintenance) {
        if (this.maintenance != maintenance) {
            //change incoming
            CloudDriver.getInstance().getEventManager().callEventGlobally(new TaskMaintenanceChangeEvent(this, maintenance));
        }
        this.maintenance = maintenance;
    }

    @Override
    public void clone(IServiceTask from) {

        this.setName(from.getName());
        this.setPermission(from.getPermission());
        this.setParent(from.getTaskGroup().getName());
        this.setNode(from.getPossibleNodes().toArray(new String[0]));
        this.setMotd(from.getMotd());

        this.setMemory(from.getMemory());
        this.setDefaultMaxPlayers(from.getDefaultMaxPlayers());
        this.setMinOnlineService(from.getMinOnlineService());
        this.setMaxOnlineService(from.getMaxOnlineService());
        this.setStartOrder(from.getStartOrder());

        this.setFallback((SimpleFallback) from.getFallback());
        this.setMaintenance(from.isMaintenance());

        this.setVersion(from.getVersion());
        this.setTemplates(from.getTemplates());

    }

    @Override
    public String replacePlaceHolders(String input) {
        input = input.replace("{task.name}", this.getName());
        input = input.replace("{task.motd}", this.getMotd());
        input = input.replace("{task.node}", this.findAnyNode().getName());

        input = input.replace("{task.memory}", String.valueOf(this.getMemory()));
        input = input.replace("{task.java}", String.valueOf(this.getJavaVersion()));
        input = input.replace("{task.capacity}", String.valueOf(this.getDefaultMaxPlayers()));

        input = input.replace("{task.version}", this.getVersion().getJar());
        input = input.replace("{task.properties}", this.getProperties().asRawJsonString());

        return input;
    }
}
