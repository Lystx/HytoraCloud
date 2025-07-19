package cloud.hytora.driver.networking;


/**
 * A {@link NetworkExecutor} combines the abilities of {@link NetworkComponent} and {@link PacketSender}
 * It creates a new type of object that indictaes that it can both act as a {@link PacketSender} and as a {@link NetworkComponent}
 *
 * @version DEV-0.2
 * @since DEV-0.2
 * @author Lystx
 *
 * @see NetworkComponent
 * @see PacketSender
 */
public interface NetworkExecutor extends NetworkComponent, PacketSender {

    //no own methods for this type
}
