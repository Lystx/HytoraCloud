package cloud.hytora.driver.networking.query;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;
import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public abstract class QueryHandler implements Consumer<QueryRequest> {

    private final UUID identifier;

    public QueryHandler() {
        this(UUID.randomUUID());
    }

    public abstract void handle(QueryRequest query);

    @Override
    public final void accept(QueryRequest tProtocolQuery) {
        this.handle(tProtocolQuery);
    }
}
