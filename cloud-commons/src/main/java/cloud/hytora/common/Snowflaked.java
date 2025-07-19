package cloud.hytora.common;

/**
 * Objects that inherit this interface
 * do have a personal identification (snowflake)
 * attachted to their name.
 *
 * @see Snowflake
 */
public interface Snowflaked {

    /**
     * @return the snowflake value for this object
     */
    long getSnowflake();
}
