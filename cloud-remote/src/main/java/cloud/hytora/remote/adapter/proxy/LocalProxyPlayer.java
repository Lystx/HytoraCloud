package cloud.hytora.remote.adapter.proxy;

import java.util.UUID;

public interface LocalProxyPlayer {

    String getName();

    UUID getUniqueId();

    void disconnect(String reason);

    void sendMessage(String message);

    String getServer();

    void setTabList(String header, String footer);

    void connect(String server);
}
