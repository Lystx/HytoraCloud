package cloud.hytora.driver.networking.query.def;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.query.*;

import java.util.*;

public class CloudQuery implements Query {

    private final Map<String, Collection<QueryHandler>> handlers;

    public CloudQuery() {
        this.handlers = new HashMap<>();

        CloudDriver.getInstance().getExecutor().registerPacketHandler(CloudQuery.this::handle);


        //default channels
        this.registerChannel(CloudDriver.Constants.QUERY_CHANNEL_PLAYER);
        this.registerChannel(CloudDriver.Constants.QUERY_CHANNEL_SERVER);
        this.registerChannel(CloudDriver.Constants.QUERY_CHANNEL_OFFLINE_PLAYER);

    }


    private void handle(PacketChannel sender, QueryPacket packet) {

        if (packet instanceof QueryResponsePacket) {
            return;
        }

        String channel = packet.getChannel();

        for (QueryHandler channelHandler : (getChannelHandlers(channel) == null ? new ArrayList<QueryHandler>() : getChannelHandlers(channel))) {

            QueryRequest request = createRequest(channel);
            request.setInternalId(packet.getInternalId());
            request.setKey(packet.getKey());
            request.setChannel(packet.getChannel());
            ((CloudQueryRequest)request).setB(packet.getPacketBuffer());
            ((CloudQueryRequest)request).setSender(sender);

            channelHandler.handle(request);
        }
    }


    @Override
    public void registerChannel(String name) {
        this.handlers.put(name, new ArrayList<>());
    }

    @Override
    public void unregisterChannel(String name) {
        this.handlers.remove(name);
    }

    @Override
    public Collection<String> getRegisteredChannels() {
        return handlers.keySet();
    }

    @Override
    public Collection<QueryHandler> getChannelHandlers(String channel) {
        return handlers.get(channel);
    }

    @Override
    public  void registerHandler(String channel, QueryHandler handler) {
        if (this.handlers.keySet().stream().noneMatch(s -> s.equalsIgnoreCase(channel))) {
            this.registerChannel(channel);
        }
        Collection<QueryHandler> channelHandlers = this.getChannelHandlers(channel);
        channelHandlers.add(handler);
        this.handlers.put(channel, channelHandlers);
    }

    @Override
    public  void unregister(String channel, QueryHandler handler) {
        Collection<QueryHandler> channelHandlers = this.getChannelHandlers(channel);
        channelHandlers.remove(handler);
        this.handlers.put(channel, channelHandlers);
    }

    @Override
    public  void unregister(String channel, UUID identifier) {
        Collection<QueryHandler> channelHandlers = this.getChannelHandlers(channel);
        channelHandlers.removeIf(h -> h.getIdentifier().equals(identifier));
        this.handlers.put(channel, channelHandlers);
    }

    @Override
    public  QueryResponse createResponse(QueryRequest request) {
        return new CloudQueryResponse(request);
    }

    @Override
    public  QueryRequest createRequest(String channel) {
        return new CloudQueryRequest(this, channel);
    }
}
