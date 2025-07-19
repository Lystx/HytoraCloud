package cloud.hytora.driver.networking;

import cloud.hytora.common.identification.ImmutableNameHolder;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;

/**
 * A {@link NetworkComponent} is a pariticpant of the cloud-network
 * It consists of its name and its {@link ConnectionType} to declare what kind of component it is
 * 
 * You can also log messages to this component using {@link #log(String, Object...)}
 * 
 * @author Lystx 
 * @version STABLE-1.0
 * @since DEV-1.5
 */
public interface NetworkComponent extends IBufferObject, ImmutableNameHolder {

    /**
     * Creates a new {@link NetworkComponent} instance
     * This instance will nowhere be cached so use it wisely.
     *
     * @param name the name of the component
     * @param type the type of the component
     * @return created instance
     */
    static NetworkComponent of(String name, ConnectionType type) {
        return new SimpleNetworkComponent(name, type);
    }

    /**
     * Creates a new {@link NetworkComponent} instance
     * This instance will nowhere be cached so use it wisely.
     * As no {@link ConnectionType} is provided it will be
     * set to {@link ConnectionType#UNKNOWN} as default
     *
     * @param name the name of the component
     * @return created instance
     */
    @Deprecated
    static NetworkComponent of(String name) {
        return new SimpleNetworkComponent(name, ConnectionType.UNKNOWN);
    }

    /**
     * @return the connection type of this component
     */
    ConnectionType getType();

    /**
     * Sends a message to the console of this component
     * This does not log into the current instances' console
     *
     * @param message the message
     * @param args the arguments to replace in message
     */
    void log(String message, Object... args);

    /**
     * Checks if a {@link NetworkComponent} matches this component
     *
     * @param component component to compare
     * @return true if matches
     */
    boolean matches(NetworkComponent component);
}
