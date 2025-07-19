package cloud.hytora.driver.networking.query.def;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.query.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@Setter
@Accessors(chain = true)
public class CloudQueryRequest implements QueryRequest {

    private final Query query;
    private UUID internalId;
    private String channel;
    private String key;
    private PacketBuffer buffer;

    private PacketChannel sender;

    public CloudQueryRequest(Query query, String channel) {
        this.query = query;
        this.channel = channel;
        this.key = "no_key_selected";
        this.internalId = UUID.randomUUID();

        this.buffer = PacketBuffer.unPooled();
    }

    @Override
    public QueryRequest setBuffer(Consumer<PacketBuffer> buffer) {
        buffer.accept(this.buffer);
        return this;
    }

    public void setB(PacketBuffer b) {
        this.buffer = b;
    }

    @Override
    public QueryResponse respond() {
        return query.createResponse(this);
    }

    @Override
    public Task<QueryResponse> executeDelayed(int ticks) {
        Task<QueryResponse> task = Task.empty();
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            Task<QueryResponse> execute = execute();
            execute.onTaskSucess(task::setResult)
                    .onTaskFailed(task::setFailure);
        }, ticks);
        return task;
    }

    @Override
    public Task<QueryResponse> execute() {
        Task<QueryResponse> responseTask = Task.empty();

        UUID queryId = UUID.randomUUID();


        //sending query request to other side to execute handler and getting response
        CloudDriver.getInstance().getExecutor().sendPacket(new QueryPacket(queryId, this.channel, this.key, this.buffer));


        CloudDriver.getInstance().getExecutor().registerConditionPacketHandler(queryPacket -> queryPacket.getInternalId().equals(queryId),
                (PacketHandler<QueryResponsePacket>) (wrapper, packet) -> {
                    UUID internalQueryId = packet.getInternalId();
                    if (internalQueryId.equals(queryId) && packet.getChannel().equalsIgnoreCase(CloudQueryRequest.this.channel)) {
                        //right packet came back

                        QueryState state = packet.getState();
                        QueryResponse response = CloudQueryRequest.this.query.createResponse(CloudQueryRequest.this);

                        response.setState(state);
                        response.setChannel(packet.getChannel());
                        response.setKey(packet.getKey());
                        ((CloudQueryResponse) response).setB(packet.getPacketBuffer());

                        if (state == QueryState.ERROR) {
                            response.setError(packet.getError());
                        }

                        responseTask.setResult(response);
                    }
                });

        return responseTask;
    }

    @Override
    public QueryResponse syncUninterruptedlyAndExecute() {
        return execute().syncUninterruptedly().orElse(null);
    }

}
