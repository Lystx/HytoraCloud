package cloud.hytora.driver.services.impl;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.ServicePingProperties;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;


@Getter
@NoArgsConstructor
@Setter
public class DefaultPingProperties implements ServicePingProperties {

    private String motd;
    private String versionText;
    private String[] playerInfo;
    private String serverIconUrl;
    private boolean usePlayerPropertiesOfService;
    private boolean isCombineAllProxiesIfProxyService;

    private int customMaxPlayers, customOnlinePlayers;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                motd = buf.readString();
                versionText = buf.readOptionalString();
                playerInfo = buf.readStringArray();
                serverIconUrl = buf.readOptionalString();
                usePlayerPropertiesOfService = buf.readBoolean();
                isCombineAllProxiesIfProxyService = buf.readBoolean();
                customMaxPlayers = buf.readInt();
                customOnlinePlayers = buf.readInt();
                break;

            case WRITE:
                buf.writeString(motd);
                buf.writeOptionalString(versionText);
                buf.writeStringArray(playerInfo);
                buf.writeOptionalString(serverIconUrl);
                buf.writeBoolean(usePlayerPropertiesOfService);
                buf.writeBoolean(isCombineAllProxiesIfProxyService);
                buf.writeInt(customMaxPlayers);
                buf.writeInt(customOnlinePlayers);
                break;
        }
    }

    @Override
    public void addPlayerInfo(String... playerInfo) {
        var strings = Arrays.asList(playerInfo);
        strings.addAll(Arrays.asList(playerInfo));
        this.playerInfo = strings.toArray(new String[0]);
    }

    @Override
    public void setCustomPlayerProperties(int maxPlayers, int onlinePlayers) {
        this.setUsePlayerPropertiesOfService(false);
        this.customMaxPlayers = maxPlayers;
        this.customOnlinePlayers = onlinePlayers;
    }
}
