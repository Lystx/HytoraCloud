package cloud.hytora.driver.networking.query.def;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.query.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.function.Consumer;

@Getter
@Setter
@Accessors(chain = true)
public class CloudQueryResponse implements QueryResponse {


    private UUID internalId;
    private String channel;
    private String key;
    private PacketBuffer buffer;
    
    private QueryState state;
    private Throwable error;

    private PacketChannel sender;
    
    public CloudQueryResponse(QueryRequest request) {
        this.channel = request.getChannel();
        this.key = request.getKey();
        
        this.buffer = PacketBuffer.unPooled();
        this.state = QueryState.FAILED;
        this.internalId = request.getInternalId();

        this.sender = ((CloudQueryRequest)request).getSender();
    }

    @Override
    public QueryResponse setBuffer(Consumer<PacketBuffer> buffer) {
        buffer.accept(this.buffer);
        return this;
    }

    public void setB(PacketBuffer b) {
        this.buffer = b;
    }


    @Override
    public QueryResponse setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    @Override
    public QueryResponse setKey(String key) {
        this.key = key;
        return this;
    }


    @Override
    public void execute() {
        QueryResponsePacket packet = new QueryResponsePacket(this.internalId, this.channel, this.key, this.buffer, this.state, this.error);

        this.sender.sendPacket(packet);
    }
}
