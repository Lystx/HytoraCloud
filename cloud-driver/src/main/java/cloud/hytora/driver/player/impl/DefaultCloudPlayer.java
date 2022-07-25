package cloud.hytora.driver.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.exception.ModuleNeededException;
import cloud.hytora.driver.exception.PlayerNotOnlineException;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.PermissionChecker;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.player.connection.PlayerConnection;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ServiceInfo;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class DefaultCloudPlayer extends DefaultCloudOfflinePlayer implements CloudPlayer {

    private ServiceInfo server;
    private ServiceInfo proxyServer;
    private PlayerConnection connection;
    private Document temporaryProperties;

    public DefaultCloudPlayer(UUID uuid, String name) {
        this(uuid, name, null, null);
    }

    public DefaultCloudPlayer(UUID uniqueId, String name, ServiceInfo server, ServiceInfo proxyServer) {
        this(uniqueId, name, System.currentTimeMillis(), System.currentTimeMillis(), DocumentFactory.newJsonDocument(), server, proxyServer);
    }

    public DefaultCloudPlayer(UUID uniqueId, String name, long firstLogin, long lastLogin, Document properties, ServiceInfo server, ServiceInfo proxyServer) {
        super(uniqueId, name,  firstLogin, lastLogin, properties);
        this.server = server;
        this.proxyServer = proxyServer;

        this.connection = new DefaultPlayerConnection(proxyServer == null ? "UNKNOWN" : proxyServer.getName(), new ProtocolAddress("127.0.0.1", -1), -1, true, false);
        this.temporaryProperties = DocumentFactory.newJsonDocument();

    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public CloudPlayer asOnlinePlayer() throws PlayerNotOnlineException {
        return this;
    }

    @Override
    public @NotNull PermissionPlayer asPermissionPlayer() throws ModuleNeededException {
        Task<PermissionManager> task = CloudDriver.getInstance().getProviderRegistry().get(PermissionManager.class);
        if (task.isNull()) {
            throw new ModuleNeededException("Permission Module");
        }
        return task.get().getPlayer(this);
    }

    @Override
    public void update() {
        CloudDriver.getInstance().getPlayerManager().updateCloudPlayer(this);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                this.uniqueId = buf.readUniqueId();
                this.name = buf.readString();

                this.temporaryProperties = buf.readDocument();
                this.connection = buf.readOptionalObject(DefaultPlayerConnection.class);

                String proxyName = buf.readOptionalString();
                String serverName = buf.readOptionalString();

                this.proxyServer = proxyName == null ? null : CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(proxyName);
                this.server = serverName == null ? null : CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(serverName);
                break;

            case WRITE:
                buf.writeUniqueId(this.uniqueId);
                buf.writeString(this.name);

                buf.writeDocument(this.temporaryProperties);
                buf.writeOptionalObject(this.connection);

                buf.writeOptionalString(this.proxyServer == null ? null : this.proxyServer.getName());
                buf.writeOptionalString(this.server == null ? null : this.server.getName());
                break;
        }
    }


    @Override
    public void cloneInternally(CloudPlayer from, CloudPlayer to) {
        to.setProxyServer(from.getProxyServer());
        to.setServer(from.getServer());

        DefaultCloudPlayer wrappedTo = (DefaultCloudPlayer) to;
        DefaultCloudPlayer wrappedFrom = (DefaultCloudPlayer) from;

        wrappedTo.setUniqueId(wrappedFrom.uniqueId);
        wrappedTo.setName(wrappedFrom.name);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        PlayerExecutor.forPlayer(this).sendMessage(message);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        PermissionChecker permissionChecker = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionChecker.class);
        return permissionChecker != null && permissionChecker.hasPermission(this.uniqueId, permission);
    }

    @NotNull
    @Override
    public CloudPlayer getPlayer() {
        return this;
    }
}
