package de.lystx.hytoracloud.driver.connection.protocol.requests.base;

import de.lystx.hytoracloud.driver.CloudDriver;
import de.lystx.hytoracloud.driver.connection.messenger.IChannelMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor @Getter
public class DriverResponseObject<T> implements DriverResponse<T> {

    private static final long serialVersionUID = 3670857554777623240L;
    /**
     * The id of this response
     */
    private String id;

    /**
     * If success
     */
    private boolean success;

    /**
     * The error
     */
    private Throwable exception;

    /**
     * The data
     */
    private T data;

    /**
     * The type class
     */
    private String typeClass;

    /**
     * Sets the data of this response
     *
     * @param data the data
     * @return current response
     */
    @Override
    public DriverResponse<T> data(Object data) {
        this.typeClass(data.getClass());
        this.data = (T) data;
        this.success = true;
        return this;
    }

    @Override @SneakyThrows
    public Class<T> typeClass() {
        return (Class<T>) Class.forName(typeClass);
    }

    @Override
    public DriverResponse<T> typeClass(Class<?> typeClass) {
        this.typeClass = typeClass.getName();
        return this;
    }
    /**
     * Sets the id of this response
     *
     * @param id the id
     * @return current response
     */
    public DriverResponseObject<T> id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public DriverResponse<T> exception(Throwable throwable) {
        this.exception = throwable;
        this.success = false;
        return this;
    }

    /**
     * Sets the success-state of this response
     *
     * @param success the state
     * @return current response
     */
    public DriverResponseObject<T> success(boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Sends this response
     */
    public void send() {
        IChannelMessage channelMessage = CloudDriver.getInstance().getRequestManager().toMessage(this);
        CloudDriver.getInstance().getMessageManager().sendChannelMessage(channelMessage);
    }

}
