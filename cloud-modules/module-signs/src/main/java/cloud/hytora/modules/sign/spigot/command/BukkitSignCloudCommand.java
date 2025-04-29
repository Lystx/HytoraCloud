package cloud.hytora.modules.sign.spigot.command;

import cloud.hytora.common.location.impl.DefaultLocation;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.sender.PlayerCommandSender;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.modules.npc.api.*;
import cloud.hytora.modules.npc.spigot.entity.npc.NPCAction;
import cloud.hytora.modules.npc.spigot.gui.CloudServiceGUI;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Command(
        value = "sign",
        permission = "cloud.modules.sign.command.use",
        executionScope = CommandScope.INGAME,
        description = "Manages the CloudSigns"
)
@Command.AutoHelp
public class BukkitSignCloudCommand {

    @Command(value = "create", description = "Creates a new cloudSign!")
    @Command.Syntax("<task>")
    public void createSign(CommandSender sender, @Command.Argument("task") IServiceTask task) {
        if (task != null) {
            if (task.getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
                sender.sendMessage("§cYou can not create §eCloudSigns §cfor §eProxies§c!");
                return;
            }

            Set<Material> materials = new HashSet<>();
            materials.add(Material.AIR);
            Location location = Bukkit.getPlayer(sender.getName()).getTargetBlock(materials, 5).getLocation();
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


    @Command(value = "gui", description = "Opens gui")
    public void onGuiOpen(CommandSender sender) {
        ICloudService cloudService = CloudDriver.getInstance().getServiceManager().thisService();



        NPCManager npcManager = CloudDriver.getInstance().getProvider(NPCManager.class);
        NPCFactory npcFactory = npcManager.getNPCFactory(cloudService);
        CloudNPCMeta meta = npcFactory.createMeta();

        meta.setDisplayName("§8» §bLobby §8┃ §7Klick mich")
                .nextId()
                .setInternalName("npc_qsg")
                .setSkinName("ByQuadrix")
                .addHologramLine(new Supplier<String>() {
                    @Override
                    public String get() {
                        IServiceTask qsg = CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask("Lobby");

                        long onlineP = qsg.getOnlineServices().stream().map(ICloudService::getOnlinePlayers).count();
                        long onlineSer = qsg.getOnlineServices().size();

                        return "§8» §7Spieler§8: §a" + onlineP + " §7Servers§8: §a" + onlineSer;
                    }
                }, TimeUnit.SECONDS, 5)
                .addHologramLine("§7This is test line")
                .setLocation(((PlayerCommandSender)sender).getPlayer().getLocation())
                .addFunction(NPCFunction.LOOK)
                .addClickAction(NPCAction.click(cloudPlayer -> {
                    cloudPlayer.sendMessage("§aClicked QSG!");
                    new CloudServiceGUI(cloudService).open(Bukkit.getPlayer(sender.getName()));
                }))
                .updateSkinAfterSpawn(1);

        CloudNPC npc = npcFactory.createNPC(meta);

        npc.spawn();

        sender.sendMessage("§aDone npc!");
    }

    @Command(value = "remove", description = "Removes the sign you're looking at!")
    public void onRemoveCloudSign(CommandSender sender) {
        Set<Material> materials = new HashSet<>();
        materials.add(Material.AIR);
        Location location = Bukkit.getPlayer(sender.getName()).getTargetBlock(materials, 5).getLocation();

        if (location.getBlock().getType().equals(Material.WALL_SIGN)) {
            ICloudSign cloudSign = ((BukkitCloudSignAPI)CloudSignAPI.getInstance()).getSignUpdater().getCloudSign(location);
            if (cloudSign == null) {
                sender.sendMessage("§cThis §eCloudSign §cseems not to be registered!");
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

            sender.sendMessage("§7You removed a CloudSign for the task §b" + cloudSign.getTaskName().toUpperCase());

        } else {
            sender.sendMessage("§cThe block you are looking at, is not a sign!");
        }
    }
}
