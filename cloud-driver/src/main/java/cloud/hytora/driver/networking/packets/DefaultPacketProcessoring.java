package cloud.hytora.driver.networking.packets;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.PacketTypeProcessor;
import cloud.hytora.driver.networking.packets.PacketRegistry;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.DefaultPacketBuffer;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.info.PacketInfo;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;

import java.io.IOException;
import java.util.UUID;

public class DefaultPacketProcessoring implements PacketTypeProcessor {
    
    @Override
    public IPacket readPacket(PacketBuffer buffer) throws IOException {
        PacketBuffer newBuffer = buffer.readBuffer();

        int index = buffer.readInt();
        Document header = buffer.readDocument();
        UUID uniqueId = buffer.readUniqueId();
        String name = buffer.readString();
        ConnectionType type = buffer.readEnum(ConnectionType.class);

        Class<? extends IPacket> packetClass = PacketRegistry.getPacketClass(index);

        if (packetClass == null) {
            return null;
        }

        IPacket packet =  ReflectionUtils.createEmpty(packetClass);

        if (packet == null) {
            throw new IllegalStateException("Couldn't construct packet for class " + packetClass);
        }

        ((AbstractPacket) packet).transferInfo(new PacketInfo(uniqueId, new SimpleNetworkComponent(name, type), header));
        ((AbstractPacket) packet).buffer(newBuffer);

        packet.applyBuffer(BufferState.READ, buffer);
        
        return packet;
    }

    @Override
    public void writePacket(IPacket packet, PacketBuffer buffer) {

        int id = PacketRegistry.getPacketId(packet.getClass());

        if (id == -1) {
            throw new NullPointerException("Couldn't find id of packet " + packet.getClass().getSimpleName());
        }

        DefaultPacketBuffer dpb = (DefaultPacketBuffer)buffer;
        
        NetworkComponent participant = dpb.getParticipant();
        
        UUID internalQueryId = packet.transferInfo().getInternalQueryId();
        String sender = participant == null ? "unknown" : participant.getName();
        ConnectionType senderType = participant == null ? ConnectionType.UNKNOWN : participant.getType();

        //writing the custom packet buffer to store data
        buffer.writeBuffer(packet.buffer());

        //writing packet id
        buffer.writeInt(id);

        //writing header
        buffer.writeDocument(packet.transferInfo().getHeader());

        //writing uuid
        buffer.writeUniqueId(internalQueryId);

        //writing sender info
        buffer.writeString(sender);
        buffer.writeEnum(senderType);

        //writing custom packet data
        try {
            packet.applyBuffer(BufferState.WRITE, buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
