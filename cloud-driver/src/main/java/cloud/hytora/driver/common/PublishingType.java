package cloud.hytora.driver.common;

/**
 * The {@link PublishingType} declares where objects
 * are being updated.
 *
 *
 * @author Lystx
 * @since STABLE-1.0
 * @version STABLE-2.0
 */
public enum PublishingType {


    /**
     * Objects will only be updated internally in cache
     */
    INTERNAL,

    /**
     * Objects will only be updated for other particpants
     * using the protocol of netty networking
     */
    PROTOCOL,

    /**
     * Objects will be updated internally in cache
     * and will be updated for all other participants
     *
     * @see #PROTOCOL
     * @see #INTERNAL
     */
    GLOBAL;


    public static PublishingType get(PublishingType[] types) {
        if (types == null || types.length == 0) {
            return GLOBAL;
        }
        return types[0];
    }


    public static PublishingType get(PublishingType[] types, PublishingType defValue) {
        if (types == null || types.length == 0) {
            return defValue;
        }
        return types[0];
    }
}
