package cloud.hytora.driver.networking.query;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import java.util.Collection;
import java.util.UUID;

public interface Query {

    static Query get() {
        return CloudDriver.getInstance().getProvider(Query.class);
    }


    void registerChannel(String name);
    void unregisterChannel(String name);
    Collection<String> getRegisteredChannels();



    Collection<QueryHandler> getChannelHandlers(String channel);
     void registerHandler(String channel, QueryHandler handler);
     void unregister(String channel, QueryHandler handler);
     void unregister(String channel, UUID identifier);

     QueryRequest createRequest(String channel);
     QueryResponse createResponse(QueryRequest request);

}
