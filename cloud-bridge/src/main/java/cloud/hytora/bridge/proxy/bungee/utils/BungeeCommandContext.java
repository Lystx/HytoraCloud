package cloud.hytora.bridge.proxy.bungee.utils;

import cloud.hytora.common.util.DisplayFormat;
import cloud.hytora.driver.commands.context.CommandContext;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeCommandContext extends CommandContext<BungeeCommandSender> {

    public BungeeCommandContext(CommandSender commandSender) {
        super(new BungeeCommandSender(commandSender));
    }
    @Override
    public void sendDisplayFormat(DisplayFormat format, BungeeCommandSender... receivers) {
        format.prepare();

        if(receivers == null || receivers.length == 0) receivers = new BungeeCommandSender[]{getCommandSender()};
        for(BungeeCommandSender receiver : receivers) {
            format.getComponents().forEach(stringBooleanPair
                    -> receiver.sendMessage(stringBooleanPair.getFirst()));
        }
    }

    @Override
    public UUID getSendersUniqueId() {
        return getCommandSender() instanceof ProxiedPlayer
                ? ((ProxiedPlayer) getCommandSender()).getUniqueId() : CONSOLE_UUID;
    }

    @Override
    protected void message(String msg, BungeeCommandSender commandSender) {
        commandSender.sendMessage(msg);
    }

}
