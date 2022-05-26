package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.tps.TickCounter;
import cloud.hytora.driver.tps.TickType;

@Command(
        name = {"tps", "tick", "ticks"},
        scope = CommandScope.CONSOLE_AND_INGAME,
        permission = "cloud.command.use"
)
@CommandDescription("Shows performance of cloud")
public class TickCommand {

    @SubCommand("")
    @CommandDescription("Shows performance of cloud")
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
