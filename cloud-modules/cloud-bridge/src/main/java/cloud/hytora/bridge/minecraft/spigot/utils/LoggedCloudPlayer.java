package cloud.hytora.bridge.minecraft.spigot.utils;

import cloud.hytora.common.location.impl.CloudLocation;
import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.common.exception.IncompatibleDriverEnvironmentException;
import cloud.hytora.driver.common.exception.ModuleNeededException;
import cloud.hytora.driver.common.exception.PlayerNotOnlineException;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.module.permission.PermissionGroup;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.connection.PlayerConnection;
import cloud.hytora.driver.entity.services.CloudService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

@AllArgsConstructor
public class LoggedCloudPlayer implements CloudPlayer {

    private final CloudPlayer cloudPlayer;
    private final Consumer<String> messageHandler;

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return cloudPlayer.hasPermission(permission);
    }

    @NotNull
    @Override
    public CloudPlayer getPlayer() {
        return cloudPlayer;
    }

    @Override
    public void sendMessage(String message) {
        messageHandler.accept(message);
    }

    @Override
    public void clone(CloudPlayer from) {
        cloudPlayer.clone(from);
    }

    @Override
    public String getMainIdentity() {
        return cloudPlayer.getMainIdentity();
    }

    @Override
    public void handleJsonOperation(BufferState state, Document document) {
        cloudPlayer.handleJsonOperation(state, document);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        cloudPlayer.applyBuffer(state, buf);
    }

    @Override
    public @NotNull String getName() {
        return cloudPlayer.getName();
    }

    @Override
    public boolean isOnline() {
        return cloudPlayer.isOnline();
    }

    @Override
    public CloudPlayer asOnlinePlayer() throws PlayerNotOnlineException {
        return cloudPlayer.asOnlinePlayer();
    }

    @Override
    public void setName(@NotNull String name) {
        cloudPlayer.setName(name);
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return cloudPlayer.getUniqueId();
    }

    @Override
    public @NotNull Document getProperties() {
        return cloudPlayer.getProperties();
    }

    @Override
    public void setProperties(@NotNull Document properties) {
        cloudPlayer.setProperties(properties);
    }

    @Nullable
    @Override
    public IEntry getProperty(String name) {
        return cloudPlayer.getProperty(name);
    }

    @Override
    public boolean hasProperty(String name) {
        return cloudPlayer.hasProperty(name);
    }

    @Override
    public void setProperty(@NotNull String name, @Nullable Object value) {
        cloudPlayer.setProperty(name, value);
    }

    @Override
    public long getFirstLogin() {
        return cloudPlayer.getFirstLogin();
    }

    @Override
    public void setFirstLogin(long time) {
        cloudPlayer.setFirstLogin(time);
    }

    @Override
    public long getLastLogin() {
        return cloudPlayer.getLastLogin();
    }

    @Override
    public void setLastLogin(long time) {
        cloudPlayer.setLastLogin(time);
    }

    @Override
    public void save() {
        cloudPlayer.save();
    }

    @NotNull
    @Override
    public CloudService getProxyServer() {
        return cloudPlayer.getProxyServer();
    }

    @Override
    public boolean isConnected() {
        return cloudPlayer.isConnected();
    }

    @Nullable
    @Override
    public CloudService getServer() {
        return cloudPlayer.getServer();
    }

    @Override
    public void setProxyServer(@NotNull CloudService service) {
        cloudPlayer.setProxyServer(service);
    }

    @Override
    public void setServer(CloudService service) {
        cloudPlayer.setServer(service);
    }

    @NotNull
    @Override
    public PlayerConnection getConnection() {
        return cloudPlayer.getConnection();
    }

    @Override
    public void setConnection(@NotNull PlayerConnection connection) {
        cloudPlayer.setConnection(connection);
    }

    @Override
    public void update(PublishingType... type) {
        cloudPlayer.update(type);
    }

    @NotNull
    @Override
    public PermissionPlayer asPermissionPlayer() throws ModuleNeededException {
        return cloudPlayer.asPermissionPlayer();
    }

    @Override
    public void sendPlainMessage(String message) {
        cloudPlayer.sendPlainMessage(message);
    }

    @Override
    public PermissionGroup getHighestPermissionGroup() {
        return cloudPlayer.getHighestPermissionGroup();
    }

    @Override
    public CloudProxyPlayer asProxyPlayer() throws IncompatibleDriverEnvironmentException {
        return cloudPlayer.asProxyPlayer();
    }

    @Override
    public CloudBukkitPlayer asBukkitPlayer() throws IncompatibleDriverEnvironmentException {
        return cloudPlayer.asBukkitPlayer();
    }
}
