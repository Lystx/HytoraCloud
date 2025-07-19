package cloud.hytora.driver.networking.query;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QueryResponsePacket extends QueryPacket {


    private QueryState state;
    private Throwable error;

    public QueryResponsePacket(UUID internalId, String channel, String key, PacketBuffer queryBuffer, QueryState state, Throwable error) {
        super(internalId, channel, key, queryBuffer);
        this.state = state;
        this.error = error;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        super.applyBuffer(state, buf);
        switch (state) {
            case WRITE:
                buf.writeOptionalEnum(this.state);
                buf.writeOptionalThrowable(this.error);

                break;

            case READ:
                this.state = buf.readOptionalEnum(QueryState.class);
                this.error = buf.readOptionalThrowable();

                break;
        }
    }
}
