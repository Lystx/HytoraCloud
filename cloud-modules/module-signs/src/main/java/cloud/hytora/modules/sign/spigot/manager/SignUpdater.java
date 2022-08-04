package cloud.hytora.modules.sign.spigot.manager;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.modules.sign.api.CloudSign;
import cloud.hytora.modules.sign.api.CloudSignGroup;
import cloud.hytora.modules.sign.api.config.SignAnimation;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import cloud.hytora.modules.sign.api.config.SignKnockbackConfig;
import cloud.hytora.modules.sign.api.config.SignLayout;
import cloud.hytora.modules.sign.spigot.BukkitCloudSignsPlugin;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;

@Getter
public class SignUpdater implements Runnable {

    /**
     * The signManager instance
     */
    private final SignManager plugin;

    /**
     * All free signs
     */
    private final Map<String , Map<Integer, CloudSign>> freeSigns;

    /**
     * The cache services
     */
    private final Map<CloudSign, String> serviceMap;

    /**
     * Scheduler stuff
     */
    private int animationsTick = 0;
    private int animationScheduler;

    public SignUpdater(SignManager plugin) {
        this.plugin = plugin;
        this.freeSigns = new HashMap<>();
        this.serviceMap = new HashMap<>();

    }

    /**
     * Loads the repeat tick
     * for the SignUpdater
     * and executes the update-Method
     */
    @Override
    public void run() {
        long repeat = plugin.getConfiguration().getLoadingLayout().getRepeatingTick();
        if (animationScheduler != 0) {
            Bukkit.getScheduler().cancelTask(this.animationScheduler);
        }
        this.animationScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitCloudSignsPlugin.getInstance(), () -> {

            try {
                SignConfiguration configuration = BukkitCloudSignsPlugin.getInstance().getSignManager().getConfiguration();

                freeSigns.clear();
                serviceMap.clear();

                for (ICloudServer service : CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.MINECRAFT)) {
                    if (!service.getServiceVisibility().equals(ServiceVisibility.INVISIBLE) && !service.getServiceState().equals(ServiceState.STOPPING)) {
                        update(service);
                    }
                }

                if (animationsTick >= configuration.getLoadingLayout().size()) {
                    animationsTick = 0;
                    return;
                }

                animationsTick++;
            } catch (Exception e) {
                e.printStackTrace();
            }

            SignConfiguration configuration = BukkitCloudSignsPlugin.getInstance().getSignManager().getConfiguration();
            SignKnockbackConfig knockBackConfig = configuration.getKnockBackConfig();
            if (!knockBackConfig.isEnabled()) {
                return;
            }
            double strength = knockBackConfig.getStrength();
            double distance = knockBackConfig.getDistance();
            Bukkit.getScheduler().runTask(BukkitCloudSignsPlugin.getInstance(), () -> {
                for (CloudSign sign : BukkitCloudSignsPlugin.getInstance().getSignManager().getCloudSigns()) {
                    World world = Bukkit.getWorld(sign.getWorld());
                    if (world == null) {
                        return;
                    }
                    Location location = new Location(world, sign.getX(), sign.getY(), sign.getZ());
                    for (Entity entity : location.getWorld().getNearbyEntities(location, distance, distance, distance)) {
                        if (entity instanceof Player && !entity.hasPermission(knockBackConfig.getByPassPermission()) && location.getBlock().getState() instanceof Sign) {
                            entity.setVelocity(new org.bukkit.util.Vector(entity.getLocation().getX() - location.getX(), entity.getLocation().getY() - location.getY(), entity.getLocation().getZ() - location.getZ()).normalize().multiply(strength).setY(0.2D));
                        }
                    }
                }
            });
        }, 0L, repeat);
    }

    /**
     * Updates Sign for a single {@link ICloudServer}
     *
     * @param current > Service to update
     */
    public void update(ICloudServer current) {
        if (current.getTask().getTaskGroup().getEnvironment().equals(SpecificDriverEnvironment.PROXY)) {
            return;
        }

        Task.runAsync(() -> {
            CloudSignGroup signGroup = new CloudSignGroup(current.getTask().getName(), BukkitCloudSignsPlugin.getInstance().getSignManager().getCloudSigns());
            Map<Integer, CloudSign> signs = signGroup.getCloudSigns();
            CloudSign cloudSign = signs.get(current.getServiceID());

            this.serviceMap.put(cloudSign, current.getName());

            if (this.freeSigns.containsKey(current.getTask().getName())) {
                Map<Integer, CloudSign> onlineSigns = this.freeSigns.get(current.getTask().getName());
                onlineSigns.put(current.getServiceID(), cloudSign);
                this.freeSigns.replace(current.getTask().getName(), onlineSigns);
            } else {
                Map<Integer, CloudSign> onlineSins = new HashMap<>();
                onlineSins.put(current.getServiceID(), cloudSign);
                this.freeSigns.put(current.getTask().getName(), onlineSins);
            }

            //Sets offline signs for current group
            Bukkit.getScheduler().runTask(BukkitCloudSignsPlugin.getInstance(), () -> {

                String group = current.getTask().getName();
                Collection<CloudSign> offlineSigns = this.getOfflineSigns(group);

                for (CloudSign sign : offlineSigns) {
                    try {

                        Location bukkitLocation = new Location(Bukkit.getWorld(sign.getWorld()), sign.getX(), sign.getY(), sign.getZ());
                        if (!bukkitLocation.getWorld().getName().equalsIgnoreCase(sign.getWorld())) {
                            return;
                        }
                        Block blockAt = Bukkit.getServer().getWorld(sign.getWorld()).getBlockAt(bukkitLocation);

                        if (!blockAt.getType().equals(Material.WALL_SIGN) || blockAt.getType().equals(Material.AIR)) {
                            return;
                        }

                        Sign bukkitSign = (Sign) blockAt.getState();
                        if (CloudDriver.getInstance().getServiceTaskManager().getTaskByNameOrNull(group) != null && CloudDriver.getInstance().getServiceTaskManager().getTaskByNameOrNull(group).isMaintenance()) {
                            this.updateBukkitSign(bukkitSign, current);
                            return;
                        }

                        SignAnimation loadingLayout = BukkitCloudSignsPlugin.getInstance().getSignManager().getConfiguration().getLoadingLayout();
                        SignLayout signLayout;
                        if (animationsTick >= BukkitCloudSignsPlugin.getInstance().getSignManager().getConfiguration().getLoadingLayout().size()) {
                            animationsTick = 0;
                            signLayout = loadingLayout.get(0);
                        } else {
                            signLayout = loadingLayout.get(animationsTick);
                        }
                        for (int i = 0; i != 4; i++) {
                            bukkitSign.setLine(i, ChatColor.translateAlternateColorCodes('&', current.getTask().replacePlaceHolders(signLayout.getLines()[i])));
                        }
                        bukkitSign.update(true);
                        Bukkit.getScheduler().runTask(BukkitCloudSignsPlugin.getInstance(), () -> this.setBlock(signLayout, bukkitSign.getLocation(), null));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                if (cloudSign != null) {
                    try {
                        Location bukkitLocation = new Location(Bukkit.getWorld(cloudSign.getWorld()), cloudSign.getX(), cloudSign.getY(), cloudSign.getZ());

                        if (!bukkitLocation.getWorld().getName().equalsIgnoreCase(cloudSign.getWorld())) {
                            return;
                        }

                        Block blockAt = Bukkit.getServer().getWorld(cloudSign.getWorld()).getBlockAt(bukkitLocation);
                        if (!blockAt.getType().equals(Material.WALL_SIGN)) {
                            return;
                        }
                        Sign sign = (Sign) blockAt.getState();
                        this.updateBukkitSign(sign, current);
                        sign.update();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            });

        });
    }



    /**
     * Loads all offline signs
     *
     * @param name the name of the group
     * @return list
     */
    public Collection<CloudSign> getOfflineSigns(String name) {

        Set<Integer> allSigns = new CloudSignGroup(name, BukkitCloudSignsPlugin.getInstance().getSignManager().getCloudSigns()).getCloudSigns().keySet();
        Set<Integer> onlineSigns = freeSigns.get(name).keySet();

        if (onlineSigns.size() == allSigns.size()) {
            return new LinkedList<>();
        } else {
            for (Integer onlineSign : onlineSigns) {
                allSigns.remove(onlineSign);
            }

            Collection<CloudSign> offlineSigns = new ArrayList<>();
            for (Integer count : allSigns) {
                CloudSign sign = new CloudSignGroup(name, BukkitCloudSignsPlugin.getInstance().getSignManager().getCloudSigns()).getCloudSigns().get(count);
                ICloudServer s = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(sign.getTask() + "-" + count);
                if (s == null || s.getServiceVisibility().equals(ServiceVisibility.INVISIBLE) || s.getServiceState().equals(ServiceState.STOPPING)) {
                    offlineSigns.add(sign);
                }
            }

            return offlineSigns;
        }
    }

    /**
     * Updates a Bukkit sign and the block behind it to
     * a given {@link SignLayout} depending on the {@link ServiceState} of the {@link ICloudServer}
     *
     * @param sign the sign
     * @param service the service
     */
    public void updateBukkitSign(Sign sign, ICloudServer service) {

        SignLayout signLayout;
        ServiceState state;
        if (service.getTask().isMaintenance()) {
            signLayout = BukkitCloudSignsPlugin.getInstance().getSignManager().getConfiguration().getMaintenanceLayout();
            state = service.getServiceState();
        } else if (service.getServiceState().equals(ServiceState.STARTING)) {
            signLayout = BukkitCloudSignsPlugin.getInstance().getSignManager().getConfiguration().getStartingLayOut();
            state = ServiceState.STARTING;
        } else if (service.getOnlinePlayerCount() >= service.getMaxPlayers()) {
            signLayout = BukkitCloudSignsPlugin.getInstance().getSignManager().getConfiguration().getFullLayout();
            state = service.getServiceState();
        } else {
            signLayout = BukkitCloudSignsPlugin.getInstance().getSignManager().getConfiguration().getOnlineLayout();
            state = ServiceState.ONLINE;
        }

        //Updating sign line
        for (int i = 0; i != 4; i++) {
            sign.setLine(i, ChatColor.translateAlternateColorCodes('&', service.replacePlaceHolders(signLayout.getLines()[i])));
        }
        sign.update(true);
        Bukkit.getScheduler().runTask(BukkitCloudSignsPlugin.getInstance(), () ->  this.setBlock(signLayout, sign.getLocation(), state));
    }


    /**
     * Sets block behind sign
     *
     * @param location the location
     * @param state the state
     */
    public void setBlock(SignLayout layout, Location location, ServiceState state) {
        Block signBlock = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Sign bukkitSign = (Sign) signBlock.getState();
        Block block;

        if (bukkitSign.getBlock().getData() == 2) {
            block = bukkitSign.getBlock().getRelative(BlockFace.SOUTH);
        } else if (bukkitSign.getBlock().getData() == 3) {
            block = bukkitSign.getBlock().getRelative(BlockFace.NORTH);
        } else if (bukkitSign.getBlock().getData() == 4) {
            block = bukkitSign.getBlock().getRelative(BlockFace.EAST);
        } else if (bukkitSign.getBlock().getData() == 5) {
            block = bukkitSign.getBlock().getRelative(BlockFace.WEST);
        } else {
            block = null;
        }

        if (block != null) {
            try {
                block.setType(Material.valueOf(layout.getBlockName()));
                block.setData((byte) layout.getSubId());
            } catch (Exception e) {
                block.setType(Material.STAINED_CLAY);
                block.setData(getBlockId(state));
            }
        }
    }

    private byte getBlockId(ServiceState state) {
        switch (state) {
            case ONLINE:
                return 4;
            case PREPARED:
                return 3;
            case STOPPING:
                return 5;
            case STARTING:
                return 2;
            default:
                return 1; //null state => offline
        }
    }

    /**
     * Filters for a {@link CloudSign} by bukkit-location
     *
     * @param location the location
     * @return sign or null
     */
    public CloudSign getCloudSign(Location location) {
        return BukkitCloudSignsPlugin.getInstance().getSignManager().getCloudSigns().stream().filter(cloudSign -> cloudSign.getX() == location.getBlockX() && cloudSign.getY() == location.getBlockY() && cloudSign.getZ() == location.getBlockZ() && cloudSign.getWorld().equalsIgnoreCase(location.getWorld().getName())).findFirst().orElse(null);
    }
}