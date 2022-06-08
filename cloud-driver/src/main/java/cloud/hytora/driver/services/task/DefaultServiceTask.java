package cloud.hytora.driver.services.task;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.node.Node;
import cloud.hytora.driver.property.ProtocolPropertyHolder;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.task.bundle.TaskGroup;
import cloud.hytora.driver.services.fallback.SimpleFallback;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.def.CloudTemplate;
import cloud.hytora.driver.services.utils.ServiceVersion;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultServiceTask extends ProtocolPropertyHolder implements ServiceTask, Bufferable {

    private String name, parent, node, motd, permission;
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
    public Node findNode() {
        return CloudDriver.getInstance().getNodeManager().getNodeByNameOrNull(this.node);
    }

    public TaskGroup getTaskGroup() {
        return CloudDriver
                .getInstance()
                .getServiceTaskManager()
                .getTaskGroupByNameOrNull(
                        this.parent);
    }

    @Override
    public List<ServiceInfo> getOnlineServices() {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByGroup(this);
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
                this.node = buf.readString();
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
                buf.writeString(this.getNode());
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
    public void cloneInternally(ServiceTask from, ServiceTask t) {
        DefaultServiceTask to = (DefaultServiceTask) t;

        to.setName(from.getName());
        to.setPermission(from.getPermission());
        to.setParent(from.getTaskGroup().getName());
        to.setNode(from.getNode());
        to.setMotd(from.getMotd());

        to.setMemory(from.getMemory());
        to.setDefaultMaxPlayers(from.getDefaultMaxPlayers());
        to.setMinOnlineService(from.getMinOnlineService());
        to.setMaxOnlineService(from.getMaxOnlineService());
        to.setStartOrder(from.getStartOrder());

        to.setFallback((SimpleFallback) from.getFallback());
        to.setMaintenance(from.isMaintenance());

        to.setVersion(from.getVersion());
        to.setTemplates(from.getTemplates());

    }

}
