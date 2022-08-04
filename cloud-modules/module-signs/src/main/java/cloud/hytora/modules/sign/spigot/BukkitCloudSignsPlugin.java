package cloud.hytora.modules.sign.spigot;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.modules.sign.api.CloudSign;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import cloud.hytora.modules.sign.api.protocol.SignProtocolType;
import cloud.hytora.modules.sign.cloud.CloudSignsModule;
import cloud.hytora.modules.sign.spigot.command.SignCommand;
import cloud.hytora.modules.sign.spigot.manager.SignManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

@Getter
public class BukkitCloudSignsPlugin extends JavaPlugin {

    @Getter
    private static BukkitCloudSignsPlugin instance;

    /**
     * The manager on bukkit side to manage signs
     */
    private SignManager signManager;

    @Override
    public void onEnable() {
        instance = this;
        this.signManager = new SignManager();

        CloudDriver.getInstance().getChannelMessenger().registerChannel(CloudSignsModule.CHANNEL_NAME, new Consumer<ChannelMessage>() {
            @Override
            public void accept(ChannelMessage channelMessage) {
                PacketBuffer buffer = channelMessage.buffer();

                switch (buffer.readEnum(SignProtocolType.class)) {
                    case SYNC_CACHE:
                        signManager.setCloudSigns(buffer.readObjectCollection(CloudSign.class));
                        break;
                    case SYNC_CONFIG:
                        signManager.setConfiguration(buffer.readDocument().toInstance(SignConfiguration.class));
                        break;
                }
            }
        });

        CloudDriver.getInstance().getCommandManager().registerCommand(new SignCommand());
    }

    @Override
    public void onDisable() {
        CloudDriver.getInstance().getCommandManager().unregisterCommand(SignCommand.class);
        CloudDriver.getInstance().getChannelMessenger().unregisterChannel(CloudSignsModule.CHANNEL_NAME);
    }
}
