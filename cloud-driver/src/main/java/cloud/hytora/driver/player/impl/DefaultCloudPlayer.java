package cloud.hytora.driver.player.impl;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.player.connection.PlayerConnection;
import cloud.hytora.driver.services.CloudServer;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class DefaultCloudPlayer extends DefaultCloudOfflinePlayer implements CloudPlayer {

    private CloudServer server;
    private CloudServer proxyServer;
    private PlayerConnection connection;
    private Document temporaryProperties;

    public DefaultCloudPlayer(UUID uuid, String name) {
        this(uuid, name, null, null);
    }

    public DefaultCloudPlayer(UUID uniqueId, String name, CloudServer server, CloudServer proxyServer) {
        this(uniqueId, name, null, System.currentTimeMillis(), System.currentTimeMillis(), DocumentFactory.newJsonDocument(), server, proxyServer);
    }

    public DefaultCloudPlayer(UUID uniqueId, String name, PlayerConnection lastConnection, long firstLogin, long lastLogin, Document properties, CloudServer server, CloudServer proxyServer) {
        super(uniqueId, name, (DefaultPlayerConnection) lastConnection, firstLogin, lastLogin, properties);
        this.server = server;
        this.proxyServer = proxyServer;

        this.connection = new DefaultPlayerConnection(proxyServer.getName(), new ProtocolAddress("127.0.0.1", -1), -1, true, false);
        this.temporaryProperties = DocumentFactory.newJsonDocument();
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
                this.connection = buf.readObject(DefaultPlayerConnection.class);

                this.proxyServer = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(buf.readString());
                this.server = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(buf.readString());
                break;

            case WRITE:
                buf.writeUniqueId(this.uniqueId);
                buf.writeString(this.name);

                buf.writeDocument(this.temporaryProperties);
                buf.writeObject(this.connection);

                buf.writeString(this.proxyServer.getName());
                buf.writeString(this.server.getName());
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

}
