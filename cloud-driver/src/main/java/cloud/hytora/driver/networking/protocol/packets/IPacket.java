package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

public interface IPacket extends IBufferObject {


    PacketTransferInfo transferInfo();

    PacketBuffer buffer();


    IPromise<Void> publishAsync();

    void setDestinationChannel(String destinationChannel);

    String getDestinationChannel();

    void publish();

    void publishTo(String... receivers);

    IPromise<Void> publishToAsync(String... receivers);

    IPromise<BufferedResponse> awaitResponse();

    IPromise<BufferedResponse> awaitResponse(String receiver);
}
