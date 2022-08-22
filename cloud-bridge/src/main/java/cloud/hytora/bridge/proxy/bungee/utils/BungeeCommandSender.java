package cloud.hytora.bridge.proxy.bungee.utils;

import cloud.hytora.driver.commands.sender.CommandSender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class BungeeCommandSender implements CommandSender {

    private final net.md_5.bungee.api.CommandSender wrapped;

    @Override
    public void sendMessage(String message) {
        wrapped.sendMessage(message);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return wrapped.hasPermission(permission);
    }

    @NotNull
    @Override
    public String getName() {
        return wrapped.getName();
    }

}
