package cloud.hytora.driver.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.gson.adapter.ExcludeJsonField;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.exception.ModuleNeededException;
import cloud.hytora.driver.exception.PlayerNotOnlineException;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.PermissionChecker;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.TemporaryProperties;
import cloud.hytora.driver.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.player.connection.PlayerConnection;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudServer;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class DefaultCloudPlayer extends DefaultCloudOfflinePlayer implements ICloudPlayer {

    @ExcludeJsonField
    private ICloudServer server;
    @ExcludeJsonField
    private ICloudServer proxyServer;
    @ExcludeJsonField
    private PlayerConnection connection;

    public DefaultCloudPlayer(UUID uuid, String name) {
        this(uuid, name, null, null);
    }

    public DefaultCloudPlayer(UUID uniqueId, String name, ICloudServer server, ICloudServer proxyServer) {
        this(uniqueId, name, System.currentTimeMillis(), System.currentTimeMillis(), server, proxyServer, DocumentFactory.newJsonDocument(), new DefaultTemporaryProperties());
    }

    public DefaultCloudPlayer(UUID uniqueId, String name, long firstLogin, long lastLogin, ICloudServer server, ICloudServer proxyServer, Document properties, TemporaryProperties temporaryProperties) {
        super(uniqueId, name,  firstLogin, lastLogin, properties, (DefaultTemporaryProperties) temporaryProperties);
        this.server = server;
        this.proxyServer = proxyServer;

        this.connection = new DefaultPlayerConnection(proxyServer == null ? "UNKNOWN" : proxyServer.getName(), new ProtocolAddress("127.0.0.1", -1), -1, true, false);
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public ICloudPlayer asOnlinePlayer() throws PlayerNotOnlineException {
        return this;
    }

    @Override
    public @NotNull PermissionPlayer asPermissionPlayer() throws ModuleNeededException {
        Task<PermissionManager> task = CloudDriver.getInstance().getProviderRegistry().get(PermissionManager.class);
        if (task.isNull()) {
            throw new ModuleNeededException("Permission Module");
        }
        return task.get().getPlayerByUniqueIdOrNull(this.uniqueId);
    }

    @Override
    public void update() {
        CloudDriver.getInstance().getPlayerManager().updateCloudPlayer(this);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        super.applyBuffer(state, buf);
        switch (state) {

            case READ:
                this.uniqueId = buf.readUniqueId();
                this.name = buf.readString();
                this.connection = buf.readOptionalObject(DefaultPlayerConnection.class);

                String proxyName = buf.readOptionalString();
                String serverName = buf.readOptionalString();

                this.proxyServer = proxyName == null ? null : CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(proxyName);
                this.server = serverName == null ? null : CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(serverName);
                break;

            case WRITE:
                buf.writeUniqueId(this.uniqueId);
                buf.writeString(this.name);

                buf.writeOptionalObject(this.connection);

                buf.writeOptionalString(this.proxyServer == null ? null : this.proxyServer.getName());
                buf.writeOptionalString(this.server == null ? null : this.server.getName());
                break;
        }
    }


    @Override
    public void clone(ICloudPlayer from) {
        this.setProxyServer(from.getProxyServer());
        this.setServer(from.getServer());

        this.setUniqueId(from.getUniqueId());
        this.setName(from.getName());
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof ICloudPlayer)) return false;
        ICloudPlayer cloudPlayer = (ICloudPlayer) obj;

        return cloudPlayer.getName().equalsIgnoreCase(this.name) && cloudPlayer.getUniqueId().equals(this.uniqueId);
    }

    @NotNull
    @Override
    public ICloudPlayer getPlayer() {
        return this;
    }
}
