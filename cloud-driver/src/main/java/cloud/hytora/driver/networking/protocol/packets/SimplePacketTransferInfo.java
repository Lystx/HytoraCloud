package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.NetworkComponent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
public class SimplePacketTransferInfo implements PacketTransferInfo {

    private UUID internalQueryId;
    private NetworkComponent sender;
    private Document document;

}
