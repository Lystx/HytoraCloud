package cloud.hytora.bridge.proxy.vera.adapter;

import cloud.hytora.remote.adapter.proxy.LocalProxyPlayer;
import de.verasoftware.proxy.api.VeraProxy;
import de.verasoftware.proxy.api.component.ChatComponent;
import de.verasoftware.proxy.api.environment.entity.player.Player;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class VeraPlayer implements LocalProxyPlayer {

    private final Player player;


    @Override
    public void setTabList(String header, String footer) {
        player.setTabHeaderAndFooter(
                ChatComponent.text(header),
                ChatComponent.text(footer)
        );
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void disconnect(String reason) {
        player.disconnect(ChatComponent.text(reason));
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public String getServer() {
        return player.getConnectedServer().getName();
    }

    @Override
    public void connect(String server) {
        player.connect(VeraProxy.getInstance().getRegisteredServer(server));
    }
}
