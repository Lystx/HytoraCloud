package cloud.hytora.driver.networking.protocol.types;

/**
 * The {@link ConnectionType} declares what kind of connnection a
 * {@link cloud.hytora.driver.networking.NetworkComponent} is .
 *
 * @author Lystx
 * @since DEV-0.1
 * @version DEV-0.1
 *
 * @see cloud.hytora.driver.networking.NetworkComponent
 * @see cloud.hytora.driver.entity.node.INode
 * @see cloud.hytora.driver.entity.services.CloudService
 */
public enum ConnectionType {

    /**
     * The connection is unknown
     */
    UNKNOWN,

    /**
     * The component is a node
     */
    NODE,

    /**
     * The component is a service
     */
    SERVICE
}
