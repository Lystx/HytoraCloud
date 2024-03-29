package cloud.hytora.node.commands.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.tps.TickCounter;
import cloud.hytora.driver.tps.TickType;
import cloud.hytora.driver.tps.ICloudTickWorker;

public class TickCommand {

    @Command(
            label = "tps",
            aliases = "ticks",
            desc = "Shows performance of cloud",
            permission = "cloud.command.tick",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void execute(CommandContext<?> ctx, CommandArguments args) {

        String s = args.get(0, String.class);
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
