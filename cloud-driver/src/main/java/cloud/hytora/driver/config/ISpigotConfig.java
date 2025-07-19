package cloud.hytora.driver.config;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface ISpigotConfig extends IBufferObject {

    default boolean isJoinMessage() {
        return !getJoinMessage().equalsIgnoreCase("none");
    }

    String getJoinMessage();

    void setJoinMessage(String joinMessage);

    boolean isWeather();

    void setWeather(boolean b);

    boolean isPeaceful();

    void setPeaceful(boolean b);

}
