package cloud.hytora.driver.services.impl;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.gson.adapter.ExcludeJsonField;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.HytoraCloudConstants;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.event.defaults.server.ServiceReadyEvent;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.packet.ServiceCommandPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.services.IProcessCloudServer;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.ServicePingProperties;
import cloud.hytora.driver.services.packet.ServiceUpdateNametagsPacket;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

import cloud.hytora.driver.services.ICloudService;
import lombok.NoArgsConstructor;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
@NoArgsConstructor
@Setter
public class UniversalCloudServer implements IProcessCloudServer, IBufferObject {

    private String task;
    private int serviceID;

    private String runningNodeName;

    private int port;
    private String hostName;

    private UUID uniqueId;
    private int maxPlayers;
    private String motd;

    @ExcludeJsonField
    private Process process;
    @ExcludeJsonField
    private File workingDirectory;

    private ServiceState serviceState = ServiceState.PREPARED;
    private ServiceVisibility serviceVisibility = ServiceVisibility.NONE;


    private long creationTimestamp; // the timestamp this ServiceInfo was created (changing any property will not influence this timestamp)
    private boolean ready;
    @ExcludeJsonField
    private Document properties; // custom properties, which are not used internally
    @ExcludeJsonField
    private DefaultPingProperties pingProperties;
    @ExcludeJsonField
    private IServiceCycleData lastCycleData;

    @ExcludeJsonField
    private PacketChannel channel;

    public UniversalCloudServer(String taskName, int id, int port, String hostname) {
        this.task = taskName;

        IServiceTask serviceTask = getTask();

        this.serviceID = id;
        this.port = port;
        this.hostName = hostname;
        this.motd = serviceTask == null ? "Default Motd" : serviceTask.getMotd();
        this.maxPlayers = serviceTask == null ? 10 : serviceTask.getDefaultMaxPlayers();

        this.creationTimestamp = System.currentTimeMillis();
        this.properties = DocumentFactory.newJsonDocument();
        this.uniqueId = UUID.randomUUID();
        this.runningNodeName = getTask().findAnyNode() == null ? "UNKNOWN" : getTask().findAnyNode().getName();

        this.pingProperties = new DefaultPingProperties();
        this.pingProperties.setMotd(this.motd);
        this.pingProperties.setUsePlayerPropertiesOfService(true);
        this.pingProperties.setCombineAllProxiesIfProxyService(true);
        this.pingProperties.setPlayerInfo(new String[0]);
        this.pingProperties.setVersionText(null);
        this.lastCycleData = new DefaultServiceCycleData(DocumentFactory.emptyDocument());
    }

    @Override
    public @NotNull String getName() {
        return this.task + "-" + this.serviceID;
    }


    @Override
    public void deploy(ServiceDeployment... deployments) {
        CloudDriver.getInstance().getTemplateManager().deployService(this, deployments);
    }

    private int lastLostCycle = -1;

    @Override
    public boolean isTimedOut() {
        long lastCycleDelay = System.currentTimeMillis() - this.lastCycleData.getTimestamp() - 30;
        int lostCycles = (int) lastCycleDelay / HytoraCloudConstants.SERVER_PUBLISH_INTERVAL;
        if (lostCycles > 0 && lostCycles != lastLostCycle) {
            lastLostCycle = lostCycles;
            CloudDriver.getInstance().getLogger().log(LogLevel.WARN, "§7The Service §8'§c{}§8' §7has not sent the required data §8[§cLost cycles: §4{} §8| §cDelay: §4{}§8]", this.getName(), lostCycles, lastCycleDelay);
        }
        return lostCycles >= HytoraCloudConstants.SERVER_MAX_LOST_CYCLES;
    }

    @Override
    public IServiceTask getTask() {
        return CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask(this.task);
    }

    @Override
    public Task<IServiceTask> getTaskAsync() {
        return CloudDriver.getInstance().getServiceTaskManager().getServiceTask(this.task);
    }

    @Override
    public boolean isRegisteredAsFallback() {
        if (getTask() == null) return false;
        return !getTask().getVersion().isProxy() && getTask().getFallback().isEnabled();
    }

    @Override
    public void updateNametags() {
        this.sendPacket(new ServiceUpdateNametagsPacket(this.getName()));
    }

    @Override
    public void shutdown() {
        CloudDriver.getInstance().getServiceManager().shutdownService(this);
    }

    @Override
    public void editPingProperties(Consumer<ServicePingProperties> ping) {
        ping.accept(this.pingProperties);
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
        UniversalCloudServer that = (UniversalCloudServer) o;

        if (this.serviceID != that.serviceID || this.port != that.port) {
            return false;
        }
        return this.task.equals(that.task);
    }

    @Override
    public void update(PublishingType... type) {
        CloudDriver.getInstance().getServiceManager().updateService(this, type);
    }


    @Override
    public void sendPacket(IPacket packet) {
        if (channel != null) {
            channel.sendPacket(packet);
            return;
        }
        CloudDriver.getInstance().getServiceManager().sendPacketToService(this, packet);
    }

    @Override
    public void setReady(boolean ready) {
        if (!this.ready && ready) { //if not ready yet but parameter is true
            CloudDriver.getInstance().getEventManager().callEvent(new ServiceReadyEvent(this.getName()), PublishingType.GLOBAL);
        }
        this.ready = ready;
    }

    @Override
    public void sendCommand(@NotNull String commandLine) {
        this.sendPacket(new ServiceCommandPacket(commandLine));
    }

    @Override
    public void sendChannelMessage(ChannelMessage message) {
        message.receiver(NetworkComponent.of(this.getName(), ConnectionType.SERVICE));

        CloudDriver.getInstance().getChannelMessenger().sendChannelMessage(message);
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
    public void setCreationTimeStamp(long creationTime) {
        this.creationTimestamp = creationTime;
    }


    @Override
    public String getReadableUptime() {
        return StringUtils.getReadableMillisDifference(this.getCreationTimestamp(), System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void clone(ICloudService from) {

        this.setServiceState(from.getServiceState());
        this.setServiceVisibility(from.getServiceVisibility());
        this.setRunningNodeName(from.getRunningNodeName());

        this.setMaxPlayers(from.getMaxPlayers());
        this.setMotd(from.getMotd());
        this.setUniqueId(from.getUniqueId());
        this.setReady(from.isReady());
        this.setCreationTimeStamp(from.getCreationTimestamp());
        this.setPingProperties((DefaultPingProperties) from.getPingProperties());
        this.setProperties(from.getProperties());
        this.setLastCycleData(from.getLastCycleData());
    }

    @Override
    public String replacePlaceHolders(String input) {
        input = input.replace("{server.name}", this.getName());
        input = input.replace("{server.motd}", this.getMotd());
        input = input.replace("{server.host}", this.getHostName());

        input = input.replace("{server.ready}", this.isReady() ? "§aYes" : "§cNo");


        input = input.replace("{server.id}", String.valueOf(this.getServiceID()));
        input = input.replace("{server.port}", String.valueOf(this.getPort()));
        input = input.replace("{server.capacity}", String.valueOf(this.getMaxPlayers()));
        input = input.replace("{server.max}", String.valueOf(this.getMaxPlayers()));
        input = input.replace("{server.online}", String.valueOf(this.getOnlinePlayerCount()));

        input = input.replace("{server.node}", this.getRunningNodeName());

        input = input.replace("{server.type}", getTask().getTaskGroup().getShutdownBehaviour().name());

        input = input.replace("{server.state}", this.getServiceState().getName());
        input = input.replace("{server.visibility}", this.getServiceVisibility().name());
        input = input.replace("{server.creationTime}", String.valueOf(this.getCreationTimestamp()));
        input = input.replace("{server.uptime}", String.valueOf(this.getReadableUptime()));
        input = input.replace("{server.uptimeDif}", String.valueOf(System.currentTimeMillis() - this.getCreationTimestamp()));
        input = input.replace("{server.uptimeDifFormat}", new SimpleDateFormat("mm:ss").format(new Date(System.currentTimeMillis() - this.getCreationTimestamp())));
        input = input.replace("{server.properties}", this.getProperties().asRawJsonString());

        return getTask().replacePlaceHolders(input);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                this.uniqueId = buf.readUniqueId();
                this.runningNodeName = buf.readString();
                this.task = buf.readString();
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
                this.pingProperties = buf.readObject(DefaultPingProperties.class);
                this.lastCycleData = buf.readObject(DefaultServiceCycleData.class);
                break;

            case WRITE:

                buf.writeUniqueId(this.getUniqueId());
                buf.writeString(this.getRunningNodeName());
                buf.writeString(this.task);
                buf.writeString(this.getHostName());
                buf.writeString(this.getMotd());

                buf.writeBoolean(this.isReady());

                buf.writeInt(this.getServiceID());
                buf.writeInt(this.getPort());
                buf.writeInt(this.getMaxPlayers());

                buf.writeEnum(this.getServiceState());
                buf.writeEnum(this.getServiceVisibility());

                buf.writeLong(this.getCreationTimestamp());
                buf.writeDocument(this.getProperties());
                buf.writeObject(this.getPingProperties());
                buf.writeObject(this.getLastCycleData());
                break;
        }
    }

    @Override
    public String getMainIdentity() {
        return uniqueId.toString();
    }
}
