package cloud.hytora.driver.message;

public interface ChannelMessageListener {


    void handleIncoming(ChannelMessage message);
}
