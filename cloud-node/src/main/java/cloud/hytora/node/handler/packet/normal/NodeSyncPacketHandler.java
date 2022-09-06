package cloud.hytora.node.handler.packet.normal;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.defaults.BufferedDocument;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.packets.defaults.GenericQueryPacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.sync.ISyncedNetworkPromise;
import cloud.hytora.driver.sync.SyncedObjectType;
import cloud.hytora.node.NodeDriver;

public class NodeSyncPacketHandler implements PacketHandler<GenericQueryPacket<?>> {


    @Override
    public void handle(PacketChannel wrapper, GenericQueryPacket<?> packet) {
        String key = packet.getKey();
        if (key.equalsIgnoreCase("cloud_internal_sync")) {
            IBufferObject request = packet.getRequest();
            if (request instanceof Document || request instanceof BufferedDocument) {

                Document document = (request instanceof Document) ? (Document)request : ((BufferedDocument)request).getWrapped();

                int id = document.get("id").toInt();

                String parameter = document.get("parameter").toString();
                SyncedObjectType<?> type = SyncedObjectType.fromId(id);

                if (type == null) {
                    throw new NullPointerException("Couldn't find any SyncedObjectType with id '" + id + "'!");
                }

                ISyncedNetworkPromise<?> object = NodeDriver.getInstance().getSyncedNetworkObject(type, parameter);
                IBufferObject syncedObjectOrNull = object.getSyncedObjectOrNull();
                packet.respond(syncedObjectOrNull);
                //responding to request of query
            } else {
                throw new IllegalStateException("Received GenericQuery with key '" + key + "' but the provided data was not a Document but a " + request.getClass().getSimpleName() + "!");
            }
        }
    }
}
