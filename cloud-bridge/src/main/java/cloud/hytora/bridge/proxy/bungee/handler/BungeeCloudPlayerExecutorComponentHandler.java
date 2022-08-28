package cloud.hytora.bridge.proxy.bungee.handler;

import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.component.SimpleComponent;
import cloud.hytora.driver.component.event.ComponentEvent;
import cloud.hytora.driver.component.event.click.ClickEvent;
import cloud.hytora.driver.component.event.hover.HoverEvent;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.packet.CloudPlayerComponentMessagePacket;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCloudPlayerExecutorComponentHandler implements PacketHandler<CloudPlayerComponentMessagePacket> {

    @Override
    public void handle(PacketChannel wrapper, CloudPlayerComponentMessagePacket packet) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
        if (player == null) {
            return;
        }
        Component message = packet.getMessage();

        player.sendMessage(this.createTextComponentFromCloudRecursive((SimpleComponent) message));
    }


    /**
     * Creates a {@link TextComponent} from a {@link SimpleComponent}
     *
     * @param chatComponent the cloudComponent
     * @return built md5 textComponent
     */
    private TextComponent createTextComponentFromCloudRecursive(SimpleComponent chatComponent) {
        TextComponent textComponent = new TextComponent(chatComponent.getContent());
        ComponentEvent<ClickEvent> clickEvent = chatComponent.getClickEvent();
        if (clickEvent != null) {
            textComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.valueOf(clickEvent.getType().name()), clickEvent.getValue()));
        }
        ComponentEvent<HoverEvent> hoverEvent = chatComponent.getHoverEvent();
        if (hoverEvent != null) {
            textComponent.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.valueOf(hoverEvent.getType().name()), new BaseComponent[]{new TextComponent(hoverEvent.getValue())}));
        }
        for (Component cloudComponent : chatComponent.getSubComponents()) {
            textComponent.addExtra(createTextComponentFromCloudRecursive((SimpleComponent) cloudComponent));
        }

        textComponent.setBold(chatComponent.isBold());
        textComponent.setItalic(chatComponent.isItalic());
        textComponent.setStrikethrough(chatComponent.isStrikeThrough());
        textComponent.setObfuscated(chatComponent.isObfuscated());
        textComponent.setUnderlined(chatComponent.isUnderlined());

        return textComponent;
    }
}
