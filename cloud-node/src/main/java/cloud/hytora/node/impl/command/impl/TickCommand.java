package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.tps.TickCounter;
import cloud.hytora.driver.tps.TickType;

@Command(
        value = "tps",
        permission = "cloud.command.use",
        description = "Shows performance of cloud",
        executionScope = CommandScope.CONSOLE_AND_INGAME
)
@ApplicationParticipant
public class TickCommand {

    @Command.Root
    public void execute(CommandSender sender) {

        sender.sendMessage("§8");
        sender.sendMessage("§7Tps§8:");
        for (TickType type : TickType.values()) {
            TickCounter tick = CloudDriver.getInstance().getTickWorker().getTick(type);
            double tps = tick.getAverage();
            sender.sendMessage("§b" + type.getLabel() + ": §7" + tps);
        }
        sender.sendMessage("§8");
    }
}
