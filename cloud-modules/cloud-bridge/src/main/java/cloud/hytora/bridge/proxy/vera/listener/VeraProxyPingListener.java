package cloud.hytora.bridge.proxy.vera.listener;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.style.ComponentColor;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.ServicePingProperties;
import cloud.hytora.remote.Remote;
import de.verasoftware.proxy.api.component.ChatComponent;
import de.verasoftware.proxy.api.event.annotation.Listener;
import de.verasoftware.proxy.api.event.defaults.PingEvent;
import de.verasoftware.proxy.api.network.ping.Favicon;
import de.verasoftware.proxy.api.network.ping.ServerPing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

public class VeraProxyPingListener {
    
    
    @Listener
    public void handle(PingEvent event) {
        ServerPing response = event.getResponse();

        ICloudService cloudServer = Remote.getInstance().thisService();
        ServicePingProperties pingProperties = cloudServer.getPingProperties();

        int maxPlayers, onlinePlayers;
        if (pingProperties.isUsePlayerPropertiesOfService()) {
            maxPlayers = cloudServer.getMaxPlayers();
            onlinePlayers = pingProperties.isCombineAllProxiesIfProxyService() ? CloudDriver.getInstance().getPlayerManager().getCloudPlayerOnlineAmount() : cloudServer.getOnlinePlayerCount();
        } else {
            maxPlayers = pingProperties.getCustomMaxPlayers();
            onlinePlayers = pingProperties.getCustomOnlinePlayers();
        }

        //player info
        String[] playerInfo = pingProperties.getPlayerInfo();
        ServerPing.Players.PlayerInfo[] info = new ServerPing.Players.PlayerInfo[playerInfo.length];
        for (int i = 0; i < playerInfo.length; i++) {
            info[i] = new ServerPing.Players.PlayerInfo(ComponentColor.translateAlternateColorCodes('&', playerInfo[i]), UUID.randomUUID());
        }

        //player values
        ServerPing.Players pp = response.getPlayers();

        pp.setSample(info);
        pp.setOnline(onlinePlayers);
        pp.setMax(maxPlayers);


        //server icon
        String serverIconUrl = pingProperties.getServerIconUrl();
        if (serverIconUrl != null) {

            try (InputStream inputStream = Files.newInputStream(new File(serverIconUrl).toPath())) {
                BufferedImage image = ImageIO.read(inputStream);
                if (image.getHeight() != 64 || image.getWidth() != 64) {
                    throw new IllegalArgumentException("ServerIcon doesn't have the size of 64x64");
                }

                response.setFavicon(Favicon.create(image));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        //motd
        response.setDescription(ChatComponent.text(ComponentColor.translateAlternateColorCodes('&', pingProperties.getMotd())));

        //protocol text
        String versionText = pingProperties.getVersionText();
        if (versionText != null && !versionText.trim().isEmpty()) {
            response.setVersion(new ServerPing.Protocol(ComponentColor.translateAlternateColorCodes('&', versionText), -1));
        }

        event.setResponse(response);
    }
}
