package cloud.hytora.node.impl.command.impl;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.common.tps.TickCounter;
import cloud.hytora.driver.common.tps.TickType;

@Command(
        value = "tps",
        permission = "cloud.hytora.command.use",
        description = "Shows performance of cloud",
        executionScope = CommandScope.CONSOLE_AND_INGAME
)

public class TickCommand {

    @Command.Root
    public void execute(CommandSender sender) {

        sender.sendMessage("§8");
        sender.sendMessage("§7Tps§8:");
        for (TickType type : TickType.values()) {
            TickCounter tick = CloudDriver.getInstance().getTickWorker().getTick(type);
            double tps = tick.getAverage();
            sender.sendMessage("§7  §8» %1" + type.getLabel() + ": §7" + tps);
        }
        sender.sendMessage("§8");
    }
}
