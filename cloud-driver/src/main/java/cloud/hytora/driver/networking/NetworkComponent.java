package cloud.hytora.driver.networking;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface NetworkComponent extends Bufferable {

    String getName();

    ConnectionType getType();

    default boolean matches(NetworkComponent component) {
        return getName().equalsIgnoreCase(component.getName()) && getType() == component.getType();
    }
}
