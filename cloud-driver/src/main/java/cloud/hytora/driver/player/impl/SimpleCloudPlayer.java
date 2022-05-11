package cloud.hytora.driver.player.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.CloudServer;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SimpleCloudPlayer implements CloudPlayer, Bufferable {

    private UUID uniqueId;
    private String username;
    private CloudServer server;
    private CloudServer proxyServer;


    public SimpleCloudPlayer(UUID uuid, String name) {
        this(uuid, name, null, null);
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
                this.username = buf.readString();
                this.proxyServer = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(buf.readString());
                this.server = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(buf.readString());
                break;

            case WRITE:
                buf.writeUniqueId(this.uniqueId);
                buf.writeString(username);
                buf.writeString(proxyServer.getName());
                buf.writeString(server.getName());
                break;
        }
    }


    @Override
    public void cloneInternally(CloudPlayer from, CloudPlayer to) {
        to.setProxyServer(from.getProxyServer());
        to.setServer(from.getServer());

        SimpleCloudPlayer wrappedTo = (SimpleCloudPlayer) to;
        SimpleCloudPlayer wrappedFrom = (SimpleCloudPlayer) from;

        wrappedTo.setUniqueId(wrappedFrom.uniqueId);
        wrappedTo.setUsername(wrappedFrom.username);
    }
}
