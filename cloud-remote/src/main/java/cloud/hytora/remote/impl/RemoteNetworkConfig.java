package cloud.hytora.remote.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.config.IConfig;
import cloud.hytora.driver.config.IDatabaseConfig;
import cloud.hytora.driver.config.INodeConfig;
import cloud.hytora.driver.config.def.UniversalDatabaseConfig;
import cloud.hytora.driver.config.def.UniversalNodeConfig;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RemoteNetworkConfig implements IConfig {

    private UniversalNodeConfig nodeConfig;
    private UniversalDatabaseConfig databaseConfig;


    @Override
    public void update() {
        CloudDriver.getInstance().getConfigManager().save(this);
    }

    @Override
    public void setDatabaseConfig(IDatabaseConfig config) {
        this.databaseConfig = (UniversalDatabaseConfig) config;
    }

    @Override
    public void setNodeConfig(INodeConfig config) {
        this.nodeConfig = (UniversalNodeConfig) config;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                nodeConfig = buf.readObject(UniversalNodeConfig.class);
                databaseConfig = buf.readObject(UniversalDatabaseConfig.class);
                break;

            case WRITE:
                buf.writeObject(nodeConfig);
                buf.writeObject(databaseConfig);
                break;
        }
    }
}
