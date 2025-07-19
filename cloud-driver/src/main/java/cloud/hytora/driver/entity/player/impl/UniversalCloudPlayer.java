package cloud.hytora.driver.entity.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.gson.adapter.ExcludeJsonField;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.component.style.ComponentColor;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.common.exception.ModuleNeededException;
import cloud.hytora.driver.common.exception.PlayerNotOnlineException;
import cloud.hytora.driver.entity.player.PlayerExtension;
import cloud.hytora.driver.entity.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.entity.player.connection.PlayerConnection;

import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.module.permission.PermissionGroup;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.module.permission.PermissionChecker;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.common.provider.ProviderNotRegisteredException;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.simplejson.api.annotation.JsonExcludeField;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class UniversalCloudPlayer extends DefaultCloudOfflinePlayer implements CloudPlayer {

    private String serverName;
    private String proxyName;
    @ExcludeJsonField
    @JsonExcludeField
    private PlayerConnection connection;

    public UniversalCloudPlayer(UUID uuid, String name) {
        this(uuid, name, null, null);
    }

    public UniversalCloudPlayer(UUID uniqueId, String name, CloudService server, CloudService proxyServer) {
        this(uniqueId, name, System.currentTimeMillis(), System.currentTimeMillis(), server, proxyServer, Document.gson());
    }

    public UniversalCloudPlayer(UUID uniqueId, String name, long firstLogin, long lastLogin, CloudService server, CloudService proxyServer, Document properties) {
        super(uniqueId, name,  firstLogin, lastLogin);
        this.properties = properties;
        this.serverName = server == null ? "" : server.getName();
        this.proxyName = proxyServer == null ? "" :proxyServer.getName();

        this.connection = new DefaultPlayerConnection(uniqueId, name, proxyServer == null ? "UNKNOWN" : proxyServer.getName(), new ProtocolAddress("127.0.0.1", -1), -1, true, false);
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
    public CloudService getServer() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(this.serverName);
    }

    @Override
    public @NotNull CloudService getProxyServer() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(this.proxyName);
    }


    @Override
    public boolean isConnected() {
        return proxyName != null && serverName != null;
    }

    @Override
    public void setProxyServer(@NotNull CloudService service) {
        this.proxyName = service.getName();
    }

    @Override
    public void setServer(CloudService service) {

        this.serverName = service == null ? "NULL" : service.getName();
    }

    @Override
    public void update(PublishingType... type) {
        CloudDriver.getInstance().getPlayerManager().updateCloudPlayer(this, type);
    }

    @Override
    public void save() {
        super.save();
        this.update();
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
    public String toString() {
        return "UniversalCloudPlayer[name=" + getName() + " uuid=" + getUniqueId() + " server=" + serverName + " proxy=" + proxyName + "]";
    }

    @Override
    public void clone(CloudPlayer f) {

        UniversalCloudPlayer from = (UniversalCloudPlayer)f;

        //offlinePlayer values
        this.setUniqueId(from.getUniqueId());
        this.setName(from.getName());
        this.setFirstLogin(from.getFirstLogin());
        this.setLastLogin(from.getLastLogin());
        this.setProperties(from.getProperties());

        //online values
        this.setProxyName(from.proxyName);
        this.setServerName(from.serverName);
        this.setConnection(from.connection);

    }

    @Override
    public void sendPlainMessage(String message) {
        message = ComponentColor.translateAlternateColorCodes('&', message);
        if (CloudDriver.getInstance().getEnvironment() == CloudDriver.Environment.SERVICE) {
            CloudService service = CloudDriver.getInstance().getServiceManager().thisService();
            if (service.getTask().getVersion().isMinecraft()) {
                asBukkitPlayer().sendMessage(message);
                return;
            }
        }
        asProxyPlayer().sendMessage(message);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        sendPlainMessage(CloudDriver.getInstance().getConfigManager().getConfig().getMessages().getPrefix() + message);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
         try {
            PermissionChecker permissionChecker = CloudDriver.getInstance().getProvider(PermissionChecker.class);
            return permissionChecker.hasPermission(this.uniqueId, permission);
        } catch (ProviderNotRegisteredException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof CloudPlayer)) return false;
        CloudPlayer cloudPlayer = (CloudPlayer) obj;

        return cloudPlayer.getName().equalsIgnoreCase(this.name) && cloudPlayer.getUniqueId().equals(this.uniqueId);
    }

    @NotNull
    @Override
    public CloudPlayer getPlayer() {
        return this;
    }

    @Override
    public CloudBukkitPlayer asBukkitPlayer() throws IncompatibleDriverEnvironmentException {
        return CloudDriver.getInstance().get(PlayerExtension.class).orThrow(new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE)).createBukkitPlayer(this);
    }

    @Override
    public CloudProxyPlayer asProxyPlayer() throws IncompatibleDriverEnvironmentException {
        return CloudDriver.getInstance().get(PlayerExtension.class).orThrow(new IncompatibleDriverEnvironmentException(CloudDriver.Environment.NODE)).createProxyPlayer(this);
    }

    @Override
    public void handleJsonOperation(BufferState state, Document document) {
        super.handleJsonOperation(state, document);
        switch (state) {
            case READ:

                break;

            case WRITE:
                document.set("proxy", getProxyServer() == null ? "Unknown" : getProxyServer().getName());
                document.set("server", getServer() == null ? "Unknown" : getServer().getName());
                break;
        }
    }

    @Override
    public PermissionGroup getHighestPermissionGroup() {
        if (!this.hasProperty("module_perms_highest_group")) {
            return null;
        }
        return this.getProperty("module_perms_highest_group").toInstance(entry -> CloudDriver.getInstance().getProvider(PermissionManager.class).getPermissionGroup(entry.toString()));
    }

    public static CloudPlayer fromOfflinePlayer(CloudOfflinePlayer player) {
        UniversalCloudPlayer cloudPlayer = new UniversalCloudPlayer();

        cloudPlayer.setUniqueId(player.getUniqueId());
        cloudPlayer.setName(player.getName());
        cloudPlayer.setFirstLogin(player.getFirstLogin());
        cloudPlayer.setLastLogin(player.getLastLogin());
        cloudPlayer.setProperties(player.getProperties());

        return cloudPlayer;
    }
    public static CloudPlayer fromOfflinePlayer(CloudOfflinePlayer player, String proxyName, String minecraftName) {
        UniversalCloudPlayer cloudPlayer = new UniversalCloudPlayer();

        cloudPlayer.setUniqueId(player.getUniqueId());
        cloudPlayer.setName(player.getName());
        cloudPlayer.setFirstLogin(player.getFirstLogin());
        cloudPlayer.setLastLogin(player.getLastLogin());
        cloudPlayer.setProperties(player.getProperties());

        cloudPlayer.setProxyName(proxyName);
        cloudPlayer.setServerName(minecraftName);
        return cloudPlayer;
    }
}
