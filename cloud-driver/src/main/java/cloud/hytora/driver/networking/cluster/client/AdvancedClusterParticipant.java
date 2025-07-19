package cloud.hytora.driver.networking.cluster.client;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.channel.*;
import lombok.Getter;

@Getter
public abstract class AdvancedClusterParticipant extends ClusterParticipant {

    public AdvancedClusterParticipant(String authKey, String clientName, ConnectionType type, Document customData) {
        super(authKey, clientName, type, customData);
    }

    public abstract void onAuthenticationChanged(PacketChannel wrapper);

    public abstract void onActivated(ChannelHandlerContext ctx);

    public abstract void onClose(ChannelHandlerContext ctx);

}
