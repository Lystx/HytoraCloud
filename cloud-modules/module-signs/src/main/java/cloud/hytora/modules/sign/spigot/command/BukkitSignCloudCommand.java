package cloud.hytora.modules.sign.spigot.command;

import cloud.hytora.common.location.impl.DefaultLocation;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.context.defaults.PlayerCommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.AllowedCommandSender;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.sender.CommandSender;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.modules.sign.api.ICloudSign;
import cloud.hytora.modules.sign.api.def.UniversalCloudSign;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.spigot.BukkitCloudSignAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Command(
        label = "sign",
        aliases = {"cloudsigns", "csigns", "signs", "cloudsign", "csign"},
        permission = "cloud.modules.sign.command.use",
        scope = CommandScope.INGAME,
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"}
)

public class BukkitSignCloudCommand {

    @Command(
            parent = "sign",
            label = "create",
            desc = "Creates a new cloudSign",
            usage = "<task>"
    )
    public void createCommand(PlayerCommandContext ctx, CommandArguments args) {
        IServiceTask task = args.get(0, IServiceTask.class);
        if (task != null) {
            if (task.getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
                ctx.sendMessage("§cYou can not create §eCloudSigns §cfor §eProxies§c!");
                return;
            }

            Set<Material> materials = new HashSet<>();
            materials.add(Material.AIR);
            Location location = Bukkit.getPlayer(ctx.getCommandSender().getName()).getTargetBlock(materials, 5).getLocation();
            if (location.getBlock().getType().equals(Material.WALL_SIGN)) {

                DefaultLocation<Integer> loc = new DefaultLocation<>((int) location.getX(), (int) location.getY(), (int) location.getZ(), location.getWorld().getName());
                UniversalCloudSign sign = new UniversalCloudSign(UUID.randomUUID(), task.getName(), loc);


                if (((BukkitCloudSignAPI)CloudSignAPI.getInstance()).getSignUpdater().getCloudSign(location) == null) {
                    Block block = Bukkit.getWorld(sign.getLocation().getWorld()).getBlockAt(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ());
                    Sign signBlock = (Sign) block.getState();
                    signBlock.setLine(0, "§8§m------");
                    signBlock.setLine(1, "§b" + task.getName().toUpperCase());
                    signBlock.setLine(2, "RELOADING...");
                    signBlock.setLine(3, "§8§m------");
                    signBlock.update(true);


                    CloudSignAPI.getInstance().getSignManager().addCloudSign(sign);
                    ctx.sendMessage("§7You created a CloudSign for the task §b" + task.getName());
                } else {
                    ctx.sendMessage("§cThe §eCloudSign §calready exists!");
                }
            } else {
                ctx.sendMessage("§cThe block you are looking at, is not a sign!");
            }
        } else {
            ctx.sendMessage("§cThis task §cdoesn't exist!");
        }
    }
    @Command(
            parent = "sign",
            label = "delete",
            desc = "Removes the sign you're looking at!"
    )
    public void deleteCommand(PlayerCommandContext ctx, CommandArguments args) {
        Set<Material> materials = new HashSet<>();
        materials.add(Material.AIR);
        Location location = Bukkit.getPlayer(ctx.getCommandSender().getName()).getTargetBlock(materials, 5).getLocation();

        if (location.getBlock().getType().equals(Material.WALL_SIGN)) {
            ICloudSign cloudSign = ((BukkitCloudSignAPI)CloudSignAPI.getInstance()).getSignUpdater().getCloudSign(location);
            if (cloudSign == null) {
                ctx.sendMessage("§cThis §eCloudSign §cseems not to be registered!");
                return;
            }
            Block block = Bukkit.getWorld(cloudSign.getLocation().getWorld()).getBlockAt(cloudSign.getLocation().getX(), cloudSign.getLocation().getY(), cloudSign.getLocation().getZ());
            Sign signBlock = (Sign) block.getState();
            signBlock.setLine(0, "§8§m------");
            signBlock.setLine(1, "§4⚠⚠⚠⚠⚠");
            signBlock.setLine(2, "§8» §cRemoved");
            signBlock.setLine(3, "§8§m------");
            signBlock.update(true);
            CloudSignAPI.getInstance().getSignManager().removeCloudSign(cloudSign);

            ctx.sendMessage("§7You removed a CloudSign for the task §b" + cloudSign.getTaskName().toUpperCase());
        } else {
            ctx.sendMessage("§cThe block you are looking at, is not a sign!");
        }
    }
}
