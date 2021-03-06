package de.lystx.hytoracloud.cloud.manager.other;

import de.lystx.hytoracloud.driver.CloudDriver;
import de.lystx.hytoracloud.driver.connection.protocol.netty.global.identification.ConnectionType;
import de.lystx.hytoracloud.driver.connection.protocol.netty.server.INetworkServer;
import de.lystx.hytoracloud.driver.connection.protocol.netty.server.NetworkServer;
import de.lystx.hytoracloud.driver.registry.ICloudService;
import de.lystx.hytoracloud.driver.registry.CloudServiceInfo;
import lombok.Getter;

import java.io.IOException;

@Getter
@CloudServiceInfo(
        name = "NetworkService",
        description = {
                "This service manages the networking stuff and starts the server for all clients"
        },
        version = 2.0
)
public class NetworkService implements ICloudService {

    private final INetworkServer networkServer;

    public NetworkService() {

        this.networkServer = new NetworkServer("127.0.0.1", CloudDriver.getInstance().getConfigManager().getNetworkConfig().getPort(), ConnectionType.CLOUD_INSTANCE);
        CloudDriver.getInstance().setInstance("connection", this.networkServer);

        new Thread(() -> {
            try {
                this.networkServer.bootstrap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "networkServer").start();
    }

    /**
     * Stops server
     */
    public void shutdown() {
        try {
            this.networkServer.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reload() {

    }

    @Override
    public void save() {

    }
}
