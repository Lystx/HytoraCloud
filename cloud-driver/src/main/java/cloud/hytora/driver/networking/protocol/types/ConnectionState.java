package cloud.hytora.driver.networking.protocol.types;

import cloud.hytora.driver.networking.NetworkComponent;

/**
 * The {@link ConnectionType} declares the state a
 * {@link NetworkComponent} is in.
 *
 * @author Lystx
 * @since DEV-0.1
 * @version DEV-0.1
 *
 * @see NetworkComponent
 */
public enum ConnectionState {

    /**
     * The component is connected
     */
    CONNECTED,

    /**
     * The component is not connected (yet)
     */
    DISCONNECTED
}
