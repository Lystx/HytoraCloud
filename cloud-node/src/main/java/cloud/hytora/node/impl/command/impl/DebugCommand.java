package cloud.hytora.node.impl.command.impl;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.annotation.Command;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.common.message.base.ChannelMessage;
import cloud.hytora.driver.common.message.IMessageChannel;
import cloud.hytora.driver.common.message.MessageListener;
import cloud.hytora.driver.entity.services.fallback.SimpleFallback;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;

@Command(
        value = "debug",
        description = "Dev Command"
)

public class DebugCommand {


    @Command.Root
    public void executeDebug(CommandSender sender) {


        System.out.println("Debug executed");
    }
}
