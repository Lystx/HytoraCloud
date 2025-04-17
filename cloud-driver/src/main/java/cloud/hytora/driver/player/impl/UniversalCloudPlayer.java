package cloud.hytora.driver.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.gson.adapter.ExcludeJsonField;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.exception.ModuleNeededException;
import cloud.hytora.driver.exception.PlayerNotOnlineException;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.PermissionChecker;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.player.connection.PlayerConnection;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudService;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class UniversalCloudPlayer extends DefaultCloudOfflinePlayer implements ICloudPlayer {

    private String serverName;
    private String proxyName;
    @ExcludeJsonField
    private PlayerConnection connection;

    public UniversalCloudPlayer(UUID uuid, String name) {
        this(uuid, name, null, null);
    }

    public UniversalCloudPlayer(UUID uniqueId, String name, ICloudService server, ICloudService proxyServer) {
        this(uniqueId, name, System.currentTimeMillis(), System.currentTimeMillis(), server, proxyServer, DocumentFactory.newJsonDocument());
    }

    public UniversalCloudPlayer(UUID uniqueId, String name, long firstLogin, long lastLogin, ICloudService server, ICloudService proxyServer, Document properties) {
        super(uniqueId, name,  firstLogin, lastLogin, properties );
        this.serverName = server == null ? "" : server.getName();
        this.proxyName = proxyServer == null ? "" :proxyServer.getName();

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


    @Nullable
    @Override
    public ICloudService getServer() {
        return CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(this.serverName);
    }

    @NotNull
    @Override
    public ICloudService getProxyServer() {
        return CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(this.proxyName);
    }

    @Override
    public Task<ICloudService> getServerAsync() {
        return CloudDriver.getInstance().getServiceManager().getServiceByNameOrNullAsync(this.serverName);
    }

    @Override
    public Task<ICloudService> getProxyServerAsync() {
        return CloudDriver.getInstance().getServiceManager().getServiceByNameOrNullAsync(this.proxyName);
    }

    @Override
    public void setProxyServer(@NotNull ICloudService service) {
        this.proxyName = service.getName();
    }

    @Override
    public void setServer(ICloudService service) {

        this.serverName = service == null ? "NULL" : service.getName();
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
                this.connection = buf.readOptionalObject(DefaultPlayerConnection.class);

                this.proxyName = buf.readOptionalString();
                this.serverName = buf.readOptionalString();

                break;

            case WRITE:

                buf.writeOptionalObject(this.connection);

                buf.writeOptionalString(this.proxyName);
                buf.writeOptionalString(this.serverName);
                break;
        }
    }


    @Override
    public void clone(ICloudPlayer from) {

        //offlinePlayer values
        this.setUniqueId(from.getUniqueId());
        this.setName(from.getName());
        this.setFirstLogin(from.getFirstLogin());
        this.setLastLogin(from.getLastLogin());
        this.setProperties(from.getProperties());

        //online values
        this.setProxyServer(from.getProxyServer());
        this.setServer(from.getServer());
        this.setConnection(from.getConnection());

    }

    @Override
    public void sendMessage(@NotNull String message) {
        PlayerExecutor.forPlayer(this).sendMessage(CloudMessages.getInstance().getPrefix() + " " + message);
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
