package cloud.hytora.plugins.smartproxy.bungee;

import cloud.hytora.common.misc.Util;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.ChannelMessageListener;
import cloud.hytora.plugins.smartproxy.bungee.listener.PlayerInjectListener;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class BungeeBootstrap extends Plugin {


    @Getter
    private final Map<InetSocketAddress, InetSocketAddress> addresses;

    @Getter
    private static BungeeBootstrap instance;

    public BungeeBootstrap() {
        instance = this;

        this.addresses = new HashMap<>();
    }

    @Override
    public void onLoad() {

        CloudDriver.getInstance().getChannelMessenger()
                .registerChannel("cloud_module_smartproxy", new ChannelMessageListener() {
                    @Override
                    public void handleIncoming(ChannelMessage message) {
                        if (message.getKey().equalsIgnoreCase("PROXY_SET_IP")) {
                            Document document = message.getDocument();

                            try {
                                InetSocketAddress client_address = Util.getAddress(document.getString("CLIENT_ADDRESS"));
                                InetSocketAddress channel_address = Util.getAddress(document.getString("CHANNEL_ADDRESS"));

                                addresses.put(channel_address, client_address);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
    }

    @Override
    public void onEnable() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new PlayerInjectListener());
    }


    @Override
    public void onDisable() {

    }
}
