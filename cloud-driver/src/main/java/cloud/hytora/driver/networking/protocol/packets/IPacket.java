package cloud.hytora.driver.networking.protocol.packets;

import cloud.hytora.common.task.ITask;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

public interface IPacket extends IBufferObject {


    PacketTransferInfo transferInfo();

    PacketBuffer buffer();


    ITask<Void> publishAsync();

    void setDestinationChannel(String destinationChannel);

    String getDestinationChannel();

    void publish();

    void publishTo(String... receivers);

    ITask<Void> publishToAsync(String... receivers);

    ITask<BufferedResponse> awaitResponse();

    ITask<BufferedResponse> awaitResponse(String receiver);
}
