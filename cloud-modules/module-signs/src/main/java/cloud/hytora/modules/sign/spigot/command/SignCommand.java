package cloud.hytora.modules.sign.spigot.command;

import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.DefaultChannelMessage;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.modules.sign.api.CloudSign;
import cloud.hytora.modules.sign.api.protocol.SignProtocolType;
import cloud.hytora.modules.sign.cloud.CloudSignsModule;
import cloud.hytora.modules.sign.spigot.BukkitCloudSignsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.HashSet;
import java.util.Set;

@Command("sign")
@CommandPermission("cloud.modules.sign.command.use")
@CommandExecutionScope(CommandScope.INGAME)
@CommandAutoHelp
public class SignCommand {


    @Command("create")
    @Syntax("<task>")
    public void createSign(CommandSender sender, @Argument("task") IServiceTask task) {
        if (task != null) {
            if (task.getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
                sender.sendMessage("§cYou can not create §eCloudSigns §cfor §eProxies§c!");
                return;
            }

            Set<Material> materials = new HashSet<>();
            materials.add(Material.AIR);
            Location location = Bukkit.getPlayer(sender.getName()).getTargetBlock(materials, 5).getLocation();
            if (location.getBlock().getType().equals(Material.WALL_SIGN)) {

                CloudSign sign = new CloudSign((int) location.getX(), (int) location.getY(), (int) location.getZ(), task.getName(), location.getWorld().getName());
                if (BukkitCloudSignsPlugin.getInstance().getSignManager().getSignUpdater().getCloudSign(location) == null) {
                    Block block = Bukkit.getWorld(sign.getWorld()).getBlockAt(sign.getX(), sign.getY(), sign.getZ());
                    Sign signBlock = (Sign) block.getState();
                    signBlock.setLine(0, "§8§m------");
                    signBlock.setLine(1, "§b" + task.getName().toUpperCase());
                    signBlock.setLine(2, "RELOADING...");
                    signBlock.setLine(3, "§8§m------");
                    signBlock.update(true);
                    BukkitCloudSignsPlugin.getInstance().getSignManager().getCloudSigns().add(sign);

                    ChannelMessage message = ChannelMessage.builder().channel(CloudSignsModule.CHANNEL_NAME).buffer(buf -> buf.writeEnum(SignProtocolType.ADD_SIGN).writeObject(sign)).build();
                    message.send();
                    sender.sendMessage("§7You created a CloudSign for the task §b" + task.getName());
                } else {
                    sender.sendMessage("§cThe §eCloudSign §calready exists!");
                }
            } else {
                sender.sendMessage("§cThe block you are looking at, is not a sign!");
            }
        } else {
            sender.sendMessage("§cThis task §cdoesn't exist!");
        }
    }
    @Command("remove")
    public void createSign(CommandSender sender) {
        Set<Material> materials = new HashSet<>();
        materials.add(Material.AIR);
        Location location = Bukkit.getPlayer(sender.getName()).getTargetBlock(materials, 5).getLocation();

        if (location.getBlock().getType().equals(Material.WALL_SIGN)) {
            CloudSign cloudSign = BukkitCloudSignsPlugin.getInstance().getSignManager().getSignUpdater().getCloudSign(location);
            if (cloudSign == null) {
                sender.sendMessage("§cThis §eCloudSign §cseems not to be registered!");
                return;
            }
            Block block = Bukkit.getWorld(cloudSign.getWorld()).getBlockAt(cloudSign.getX(), cloudSign.getY(), cloudSign.getZ());
            Sign signBlock = (Sign) block.getState();
            signBlock.setLine(0, "§8§m------");
            signBlock.setLine(1, "§4⚠⚠⚠⚠⚠");
            signBlock.setLine(2, "§8» §cRemoved");
            signBlock.setLine(3, "§8§m------");
            signBlock.update(true);
            BukkitCloudSignsPlugin.getInstance().getSignManager().getCloudSigns().remove(cloudSign);

            ChannelMessage message = ChannelMessage.builder().channel(CloudSignsModule.CHANNEL_NAME).buffer(buf -> buf.writeEnum(SignProtocolType.REMOVE_SIGN).writeObject(cloudSign)).build();
            message.send();

            sender.sendMessage("§7You removed a CloudSign for the task §b" + cloudSign.getTask().toUpperCase());

        } else {
            sender.sendMessage("§cThe block you are looking at, is not a sign!");
        }
    }
}
