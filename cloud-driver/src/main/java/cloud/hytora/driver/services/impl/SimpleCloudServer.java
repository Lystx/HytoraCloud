package cloud.hytora.driver.services.impl;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.exception.IncompatibleDriverEnvironment;
import cloud.hytora.driver.networking.packets.services.CloudServerCommandPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.services.NodeCloudServer;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

import cloud.hytora.driver.services.CloudServer;
import lombok.NoArgsConstructor;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.zeroturnaround.exec.ProcessResult;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Getter
@NoArgsConstructor
@Setter
public class SimpleCloudServer implements NodeCloudServer, Bufferable {

    private ServerConfiguration configuration;
    private int serviceID;

    private int port;
    private String hostName;
    private int maxPlayers;
    private String motd;

    private Process process;
    private ProcessResult processResult;
    private File workingDirectory;

    private ServiceState serviceState = ServiceState.PREPARED;
    private ServiceVisibility serviceVisibility = ServiceVisibility.NONE;

    private long creationTimestamp; // the timestamp this ServiceInfo was created (changing any property will not influence this timestamp)
    private boolean screenServer;
    private boolean ready;
    private Document properties; // custom properties, which are not used internally

    public SimpleCloudServer(String group, int id, int port, String hostname) {
        this.configuration = CloudDriver.getInstance().getConfigurationManager().getConfigurationByNameOrNull(group);
        this.serviceID = id;
        this.port = port;
        this.hostName = hostname;
        this.motd = configuration == null ? "Default Motd" : configuration.getMotd();
        this.maxPlayers = configuration == null ? 10 : configuration.getDefaultMaxPlayers();

        this.creationTimestamp = System.currentTimeMillis();
        this.properties = DocumentFactory.newJsonDocument();
    }

    @Override
    public @NotNull String getName() {
        if (this.configuration == null) {
            return "UNKNOWN" + "-" + this.serviceID;
        }
        return this.configuration.getName() + "-" + this.serviceID;
    }

    @Override
    public NodeCloudServer asCloudServer() throws IncompatibleDriverEnvironment {
        IncompatibleDriverEnvironment.throwIfNeeded(DriverEnvironment.NODE);
        return this;
    }

    @Override
    public List<String> queryServiceOutput() {
        return CloudDriver.getInstance().getServiceManager().queryServiceOutput(this);
    }

    @Override
    public void deploy(ServiceDeployment... deployments) {
        CloudDriver.getInstance().getTemplateManager().deployService(this, deployments);
    }

    @Override
    public boolean isTimedOut() {
        long lastCycleDelay = System.currentTimeMillis() - this.creationTimestamp - 30;
        int lostCycles = (int) lastCycleDelay / CloudDriver.SERVER_PUBLISH_INTERVAL;
        if (lostCycles > 0) {
            CloudDriver.getInstance().getLogger().warn("Service timeout " + this.getName() + ": lost {} cycles ({}ms)", lostCycles, lastCycleDelay);
        }
        return lostCycles >= CloudDriver.SERVER_CYCLE_TIMEOUT;
    }

    @Override
    public void edit(@NotNull Consumer<CloudServer> serviceConsumer) {
        serviceConsumer.accept(this);
        this.update();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleCloudServer that = (SimpleCloudServer) o;

        if (this.serviceID != that.serviceID || this.port != that.port) {
            return false;
        }
        return this.configuration.equals(that.configuration);
    }

    public void update() {
        CloudDriver.getInstance().getServiceManager().updateService(this);
    }

    @Override
    public void sendPacket(IPacket packet) {
        CloudDriver.getInstance().getServiceManager().sendPacketToService(this, (Packet) packet);
    }

    @Override
    public void executeCommand(@NotNull String commandLine) {
        this.sendPacket(new CloudServerCommandPacket(commandLine));
    }

    @Override
    public ConnectionType getType() {
        return ConnectionType.SERVICE;
    }

    @Override
    public void log(String message, Object... args) {
        CloudDriver.getInstance().logToExecutor(this, message, args);
    }

    @Override
    public void cloneInternally(CloudServer from, CloudServer to) {

        to.setServiceState(from.getServiceState());
        to.setServiceVisibility(from.getServiceVisibility());

        to.setMaxPlayers(from.getMaxPlayers());
        to.setMotd(from.getMotd());
        to.setReady(from.isReady());
        ((SimpleCloudServer)to).setCreationTimeStamp(from.getCreationTimestamp());
        to.setProperties(from.getProperties());
    }

    @Override
    public String getReadableUptime() {
        return StringUtils.getReadableMillisDifference(this.getCreationTimestamp(), System.currentTimeMillis());
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                this.configuration = CloudDriver.getInstance().getConfigurationManager().getConfigurationByNameOrNull(buf.readString());
                this.hostName = buf.readString();
                this.motd = buf.readString();

                this.ready = buf.readBoolean();

                this.serviceID = buf.readInt();
                this.port = buf.readInt();
                this.maxPlayers = buf.readInt();

                this.serviceState = buf.readEnum(ServiceState.class);
                this.serviceVisibility = buf.readEnum(ServiceVisibility.class);

                this.creationTimestamp = buf.readLong();
                this.properties = buf.readDocument();
                break;

            case WRITE:

                buf.writeString(this.getConfiguration().getName());
                buf.writeString(this.getHostName());
                buf.writeString(this.getMotd());

                buf.writeBoolean(this.isReady());

                buf.writeInt(this.getServiceID());
                buf.writeInt(this.getPort());
                buf.writeInt(this.getMaxPlayers());

                buf.writeEnum(this.getServiceState());
                buf.writeEnum(this.getServiceVisibility());

                buf.writeLong(this.getCreationTimestamp());
                buf.writeDocument(this.properties);
                break;
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void setCreationTimeStamp(long creationTime) {
        this.creationTimestamp = creationTime;
    }
}
