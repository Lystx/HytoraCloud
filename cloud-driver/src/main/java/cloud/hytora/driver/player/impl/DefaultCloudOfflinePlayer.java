package cloud.hytora.driver.player.impl;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.exception.PlayerNotOnlineException;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerUnsafe;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;


@NoArgsConstructor
@Getter
@AllArgsConstructor
@Setter
@ToString
public class DefaultCloudOfflinePlayer implements CloudOfflinePlayer {

    protected UUID uniqueId;
    protected String name;
    protected long firstLogin;
    protected long lastLogin;
    protected Document properties;


    public DefaultCloudOfflinePlayer(Document data) {
        this.applyDocument(data);
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
    public void editProperties(Consumer<Document> properties) {
        properties.accept(this.properties);

        this.saveOfflinePlayer();
    }


    @Deprecated
    public PlayerUnsafe unsafe() {
        return new DefaultPlayerUnsafe(this);
    }

    @Override
    public boolean isOnline() {
        return CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(this.uniqueId) != null;
    }

    @Override
    public ICloudPlayer asOnlinePlayer() throws PlayerNotOnlineException {
        if (this.isOnline()) {
            return CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(this.uniqueId);
        }
        throw new PlayerNotOnlineException();
    }

    @Override
    public void saveOfflinePlayer() {
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
    public Document toDocument() {
        if (properties == null) {
            properties = Document.newJsonDocument();
        }

        properties = Document.newJsonDocument(properties.toString());

        return Document.newJsonDocument()
                .set("name", this.name)
                .set("uniqueId", this.uniqueId)
                .set("firstLogin", firstLogin)
                .set("lastLogin", lastLogin)
                .set("properties", properties.asRawJsonString())
                ;
    }

    @Override
    public void applyDocument(Document document) {
        this.name = document.getString("name");
        this.uniqueId = document.getUniqueId("uniqueId");
        this.firstLogin = document.getLong("firstLogin");
        this.lastLogin = document.getLong("lastLogin");
      //  this.properties = Document.newJsonDocument(document.getDocument("properties").toString());
        this.properties = Document.newJsonDocument(document.getString("properties"));
    }
}
