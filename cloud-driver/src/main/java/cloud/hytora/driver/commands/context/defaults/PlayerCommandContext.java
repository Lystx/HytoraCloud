package cloud.hytora.driver.commands.context.defaults;

import cloud.hytora.common.task.ITask;
import cloud.hytora.common.util.DisplayFormat;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.context.MessageContextResult;
import cloud.hytora.driver.commands.sender.PlayerCommandSender;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.ICloudPlayerManager;

import java.util.UUID;

public class PlayerCommandContext extends CommandContext<PlayerCommandSender> {

    public PlayerCommandContext(PlayerCommandSender commandSender) {
        super(commandSender);
    }

    @Override
    public UUID getSendersUniqueId() {
        return getCommandSender().getUniqueId();
    }

    public ICloudPlayer getPlayer() {
        return getCommandSender().getPlayer();
    }

    public ITask<ICloudPlayer> getPlayerAsync() {
        return ITask.callAsync(() ->
                CloudDriver
                        .getInstance()
                        .getProviderRegistry()
                        .getUnchecked(ICloudPlayerManager.class)
                        .getCloudPlayerByNameOrNull(getCommandSender().getName()));
    }

    @Override
    protected void message(String msg, PlayerCommandSender target) {
        target.sendMessage(msg);
    }


    // TODO: 20.08.2022
    @Override
    public MessageContextResult<PlayerCommandSender> sendMessage(boolean condition) {
        return super.sendMessage(condition);
    }

    @Override
    public MessageContextResult<PlayerCommandSender> sendMessage(String msg, Object... replacements) {
        return super.sendMessage(msg, replacements);
    }

    @Override
    public MessageContextResult<PlayerCommandSender> sendMessage(boolean condition, String msg, Object... replacements) {
        return super.sendMessage(condition, msg, replacements);
    }

    @Override
    public void sendDisplayFormat(DisplayFormat format, PlayerCommandSender... receivers) {
        format.getComponents().forEach(stringBooleanPair -> {
            for (PlayerCommandSender receiver : receivers) {
                message(stringBooleanPair.getFirst(), receiver);
            }
        });
    }
}
