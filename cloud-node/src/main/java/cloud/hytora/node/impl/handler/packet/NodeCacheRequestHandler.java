package cloud.hytora.node.impl.handler.packet;

import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheRequest;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

import java.io.IOException;


public class NodeCacheRequestHandler implements PacketHandler<PacketDriverCacheRequest> {

    @Override
    public void handle(PacketChannel channel, PacketDriverCacheRequest packet) {

        packet.sendResponse()
                .setState(NetworkResponseState.OK)
                .setBuffer(buf -> {
                    try {
                        buf.writePacket(new PacketDriverCacheUpdate());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .execute();

    }
}
