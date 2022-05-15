package cloud.hytora.driver.services.configuration;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.fallback.SimpleFallback;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.ServiceVersion;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleServerConfiguration implements ServerConfiguration, Bufferable {

    private String permission;
    private String name, template, node, motd;
    private int memory, defaultMaxPlayers, minOnlineService, maxOnlineService, startOrder, javaVersion;
    private boolean maintenance;
    private SimpleFallback fallback = new SimpleFallback();
    private ConfigurationDownloadEntry[] startupDownloadEntries = new ConfigurationDownloadEntry[0];

    private ServiceVersion version;
    private ServiceShutdownBehaviour shutdownBehaviour;

    @Override
    public List<CloudServer> getOnlineServices() {
        return CloudDriver.getInstance().getServiceManager().getAllServicesByGroup(this);
    }

    @Override
    public void update() {
        CloudDriver.getInstance().getConfigurationManager().update(this);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                this.name = buf.readString();
                this.permission = buf.readString();
                this.template = buf.readString();
                this.node = buf.readString();
                this.motd = buf.readString();

                this.memory = buf.readInt();
                this.defaultMaxPlayers = buf.readInt();
                this.minOnlineService = buf.readInt();
                this.maxOnlineService = buf.readInt();
                this.startOrder = buf.readInt();
                this.javaVersion = buf.readInt();

                this.fallback = buf.readObject(SimpleFallback.class);
                this.startupDownloadEntries = buf.readObjectArray(ConfigurationDownloadEntry.class);
                this.maintenance = buf.readBoolean();

                this.version = buf.readEnum(ServiceVersion.class);
                this.shutdownBehaviour = buf.readEnum(ServiceShutdownBehaviour.class);
                break;

            case WRITE:
                buf.writeString(this.getName());
                buf.writeString(this.getPermission());
                buf.writeString(this.getTemplate());
                buf.writeString(this.getNode());
                buf.writeString(this.getMotd());

                buf.writeInt(this.getMemory());
                buf.writeInt(this.getDefaultMaxPlayers());
                buf.writeInt(this.getMinOnlineService());
                buf.writeInt(this.getMaxOnlineService());
                buf.writeInt(this.getStartOrder());
                buf.writeInt(this.getJavaVersion());

                buf.writeObject(this.getFallback());
                buf.writeObjectArray(this.getStartupDownloadEntries());
                buf.writeBoolean(this.isMaintenance());

                buf.writeEnum(this.getVersion());
                buf.writeEnum(this.getShutdownBehaviour());
                break;
        }
    }
    @Override
    public void cloneInternally(ServerConfiguration from, ServerConfiguration t) {
        SimpleServerConfiguration to = (SimpleServerConfiguration) t;

        to.setName(from.getName());
        to.setPermission(from.getPermission());
        to.setTemplate(from.getTemplate());
        to.setNode(from.getNode());
        to.setMotd(from.getMotd());

        to.setMemory(from.getMemory());
        to.setDefaultMaxPlayers(from.getDefaultMaxPlayers());
        to.setMinOnlineService(from.getMinOnlineService());
        to.setMaxOnlineService(from.getMaxOnlineService());
        to.setStartOrder(from.getStartOrder());

        to.setFallback((SimpleFallback) from.getFallback());
        to.setStartupDownloadEntries(from.getStartupDownloadEntries());
        to.setMaintenance(from.isMaintenance());

        to.setVersion(from.getVersion());
        to.setShutdownBehaviour(from.getShutdownBehaviour());

    }

}
