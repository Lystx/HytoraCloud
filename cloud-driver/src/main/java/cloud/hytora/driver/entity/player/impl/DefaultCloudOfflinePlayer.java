package cloud.hytora.driver.entity.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.exception.ModuleNeededException;
import cloud.hytora.driver.common.exception.PlayerNotOnlineException;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.common.property.AbstractPropertyHolder;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;


@NoArgsConstructor
@Getter
@AllArgsConstructor
@Setter
@ToString
public class DefaultCloudOfflinePlayer extends AbstractPropertyHolder implements CloudOfflinePlayer {

    protected UUID uniqueId;
    protected String name;
    protected long firstLogin;
    protected long lastLogin;


    public DefaultCloudOfflinePlayer(Document data) {
        this.handleJsonOperation(BufferState.READ, data);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buffer) throws IOException {
        switch (state) {
            case WRITE:
                buffer.writeUniqueId(uniqueId);
                buffer.writeString(name);
                buffer.writeLong(firstLogin);
                buffer.writeLong(lastLogin);
                buffer.writeDocument(properties);
                break;

            case READ:
                uniqueId = buffer.readUniqueId();
                name = buffer.readString();
                firstLogin = buffer.readLong();
                lastLogin = buffer.readLong();
                properties = buffer.readDocument();
                break;
        }
    }


    @Override
    public boolean isOnline() {
        return CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(this.uniqueId) != null;
    }

    @Override
    public CloudPlayer asOnlinePlayer() throws PlayerNotOnlineException {
        if (this.isOnline()) {
            return CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(this.uniqueId);
        }
        throw new PlayerNotOnlineException();
    }


    @Override
    public @NotNull PermissionPlayer asPermissionPlayer() throws ModuleNeededException {
        Task<PermissionManager> task = CloudDriver.getInstance().get(PermissionManager.class);
        if (task.isNull()) {
            throw new ModuleNeededException("Permission Module");
        }
        return task.get().getPermissionPlayer(this.uniqueId);
    }


    @Override
    public boolean hasPermission(String perm) {
        PermissionManager permissionManager = CloudDriver.getInstance().getProvider(PermissionManager.class);
        if (permissionManager == null) {
            return false;
        }
        return permissionManager.hasPermission(this.getUniqueId(), perm);
    }

    @Override
    public void save() {
        CloudDriver.getInstance().getPlayerManager().saveOfflinePlayer(this);
    }

    @Nonnull
    @Override
    public Document getProperties() {
        return properties;
    }

    @Override
    public String getMainIdentity() {
        return uniqueId.toString();
    }


    @Override
    public void handleJsonOperation(BufferState state, Document document) {
        switch (state) {
            case READ:
                this.name = document.getString("name");
                this.uniqueId = document.getUniqueId("uniqueId");
                this.firstLogin = document.getLong("firstLogin");
                this.lastLogin = document.getLong("lastLogin");
                //  this.properties = Document.newJsonDocument(document.getDocument("properties").toString());
                this.properties = Document.gson(document.getString("properties"));
                break;

            case WRITE:

                if (properties == null) {
                    properties = Document.gson();
                }
                properties = Document.gson(properties.toString());
                document.set("name", this.name);
                document.set("uniqueId", this.uniqueId);
                document.set("firstLogin", firstLogin);
                document.set("lastLogin", lastLogin);
                document.set("properties", properties.asRawJsonString());
                break;
        }
    }

}
