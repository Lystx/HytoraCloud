package cloud.hytora.bridge.minecraft.spigot.utils;

import cloud.hytora.common.location.impl.CloudEntityLocation;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.exception.ModuleNeededException;
import cloud.hytora.driver.exception.PlayerNotOnlineException;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerUnsafe;
import cloud.hytora.driver.player.connection.PlayerConnection;
import cloud.hytora.driver.player.impl.DefaultPlayerUnsafe;
import cloud.hytora.driver.services.ICloudService;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;

@AllArgsConstructor
public class LoggedCloudPlayer implements ICloudPlayer {

    private final ICloudPlayer cloudPlayer;
    private final Consumer<String> messageHandler;

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return cloudPlayer.hasPermission(permission);
    }

    @NotNull
    @Override
    public ICloudPlayer getPlayer() {
        return cloudPlayer;
    }

    @Override
    public void sendMessage(String message) {
        messageHandler.accept(message);
    }

    @Override
    public Document toDocument() {
        return cloudPlayer.toDocument();
    }

    @Override
    public void applyDocument(Document document) {
        cloudPlayer.applyDocument(document);
    }

    @Override
    public void clone(ICloudPlayer from) {
        cloudPlayer.clone(from);
    }

    @Override
    public String getMainIdentity() {
        return cloudPlayer.getMainIdentity();
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
    public ICloudPlayer asOnlinePlayer() throws PlayerNotOnlineException {
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

    @Override
    public void editProperties(Consumer<Document> properties) {
        cloudPlayer.editProperties(properties);
    }

    @Override
    public PlayerUnsafe unsafe() {
        return new DefaultPlayerUnsafe(this);
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
    public void saveOfflinePlayer() {
        cloudPlayer.saveOfflinePlayer();
    }

    @NotNull
    @Override
    public ICloudService getProxyServer() {
        return cloudPlayer.getProxyServer();
    }

    @Override
    public Task<ICloudService> getProxyServerAsync() {
        return cloudPlayer.getProxyServerAsync();
    }

    @Override
    public CloudEntityLocation<Double, Float> getLocation() {
        return cloudPlayer.getLocation();
    }

    @Nullable
    @Override
    public ICloudService getServer() {
        return cloudPlayer.getServer();
    }

    @Override
    public Task<ICloudService> getServerAsync() {
        return cloudPlayer.getServerAsync();
    }

    @Override
    public void setProxyServer(@NotNull ICloudService service) {
        cloudPlayer.setProxyServer(service);
    }

    @Override
    public void setServer(ICloudService service) {
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
}
