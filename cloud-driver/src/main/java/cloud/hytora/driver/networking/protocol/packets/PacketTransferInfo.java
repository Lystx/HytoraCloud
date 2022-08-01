package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.NetworkComponent;

import java.util.UUID;

public interface PacketTransferInfo {

    UUID getInternalQueryId();

    void setInternalQueryId(UUID queryId);

    NetworkComponent getSender();

    Document getDocument();

}
