package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.networking.protocol.codec.buf.defaults.BufferedLong;
import cloud.hytora.driver.networking.protocol.codec.buf.defaults.BufferedString;
import cloud.hytora.driver.networking.protocol.packets.defaults.GenericQueryPacket;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.node.INode;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.tps.TickCounter;
import cloud.hytora.driver.tps.TickType;
import cloud.hytora.driver.tps.ICloudTickWorker;

public class TickCommand {

    @Command(label = "tps", aliases = "ticks", desc = "Shows performance of cloud")
    public void execute(CommandContext<?> ctx, CommandArguments args) {

        ctx.sendMessage("§8");
        ctx.sendMessage("§7Tps§8:");
        for (TickType type : TickType.values()) {
            TickCounter tick = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudTickWorker.class).getTick(type);
            double tps = tick.getAverage();
            ctx.sendMessage("§b" + type.getLabel() + ": §7" + tps);
        }
        ctx.sendMessage("§8");
    }
}
