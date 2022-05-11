package cloud.hytora.driver.networking.cluster.client;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import io.netty.channel.Channel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimpleClusterClientExecutor extends SimpleNetworkComponent implements ClusterClientExecutor {


    private final Channel channel;
    private Document data;
    private boolean authenticated;

    public SimpleClusterClientExecutor(Channel channel) {
        super("UNKNOWN", ConnectionType.UNKNOWN);
        this.channel = channel;
        this.authenticated = false;
        this.name = "UNKNOWN";
        this.data = DocumentFactory.newJsonDocument();
    }

}
