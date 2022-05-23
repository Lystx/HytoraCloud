package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface NetworkComponent extends Bufferable {

    static NetworkComponent of(String name, ConnectionType type) {
        return new SimpleNetworkComponent(name, type);
    }

    @Deprecated
    static NetworkComponent of(String name) {
        return new SimpleNetworkComponent(name, ConnectionType.UNKNOWN);
    }

    String getName();

    ConnectionType getType();

    void log(String message, Object... args);

    default boolean matches(NetworkComponent component) {
        return getName().equalsIgnoreCase(component.getName()) && (getType() == component.getType() || getType() == ConnectionType.UNKNOWN);
    }
}
