package cloud.hytora.modules.sign.spigot.manager;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.modules.sign.api.ICloudSign;
import cloud.hytora.modules.sign.api.def.UniversalCloudSign;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.config.SignAnimation;
import cloud.hytora.modules.sign.api.config.SignConfiguration;
import cloud.hytora.modules.sign.api.config.SignKnockbackConfig;
import cloud.hytora.modules.sign.api.config.SignLayout;
import cloud.hytora.modules.sign.spigot.BukkitBootstrap;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class SignUpdater implements Runnable {

    /**
     * All free signs
     */
    private final Map<String , Map<Integer, ICloudSign>> freeSigns;

    /**
     * The cache services
     */
    private final Map<ICloudSign, String> serviceMap;

    /**
     * Scheduler stuff
     */
    private int animationsTick = 0;
    private int animationScheduler;

    public SignUpdater() {
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
        long repeat = CloudSignAPI.getInstance().getSignConfiguration().getLoadingLayout().getRepeatingTick();
        if (animationScheduler != 0) {
            Bukkit.getScheduler().cancelTask(this.animationScheduler);
        }
        this.animationScheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitBootstrap.getInstance(), () -> {

            try {
                SignConfiguration configuration = CloudSignAPI.getInstance().getSignConfiguration();

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

            SignConfiguration configuration = CloudSignAPI.getInstance().getSignConfiguration();
            SignKnockbackConfig knockBackConfig = configuration.getKnockBackConfig();
            if (!knockBackConfig.isEnabled()) {
                return;
            }
            double strength = knockBackConfig.getStrength();
            double distance = knockBackConfig.getDistance();
            Bukkit.getScheduler().runTask(BukkitBootstrap.getInstance(), () -> {
                for (ICloudSign sign : CloudSignAPI.getInstance().getSignManager().getAllCachedCloudSigns()) {
                    World world = Bukkit.getWorld(sign.getLocation().getWorld());
                    if (world == null) {
                        return;
                    }
                    Location location = new Location(world, sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ());
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
            BukkitCloudSignGroup signGroup = new BukkitCloudSignGroup(current.getTask().getName(), CloudSignAPI.getInstance().getSignManager().getAllCachedCloudSigns());
            Map<Integer, ICloudSign> signs = signGroup.getCloudSigns();
            ICloudSign cloudSign = signs.get(current.getServiceID());

            this.serviceMap.put(cloudSign, current.getName());

            if (this.freeSigns.containsKey(current.getTask().getName())) {
                Map<Integer, ICloudSign> onlineSigns = this.freeSigns.get(current.getTask().getName());
                onlineSigns.put(current.getServiceID(), cloudSign);
                this.freeSigns.replace(current.getTask().getName(), onlineSigns);
            } else {
                Map<Integer, ICloudSign> onlineSigns = new HashMap<>();
                onlineSigns.put(current.getServiceID(), cloudSign);
                this.freeSigns.put(current.getTask().getName(), onlineSigns);
            }

            //Sets offline signs for current group
            Bukkit.getScheduler().runTask(BukkitBootstrap.getInstance(), () -> {

                String group = current.getTask().getName();
                Collection<ICloudSign> offlineSigns = this.getOfflineSigns(group);

                for (ICloudSign sign : offlineSigns) {
                    try {

                        Location bukkitLocation = new Location(Bukkit.getWorld(sign.getLocation().getWorld()), sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ());
                        if (!bukkitLocation.getWorld().getName().equalsIgnoreCase(sign.getLocation().getWorld())) {
                            return;
                        }
                        Block blockAt = Bukkit.getServer().getWorld(sign.getLocation().getWorld()).getBlockAt(bukkitLocation);

                        if (!blockAt.getType().equals(Material.WALL_SIGN) || blockAt.getType().equals(Material.AIR)) {
                            return;
                        }

                        Sign bukkitSign = (Sign) blockAt.getState();
                        if (CloudDriver.getInstance().getServiceTaskManager().getTaskByNameOrNull(group) != null && CloudDriver.getInstance().getServiceTaskManager().getTaskByNameOrNull(group).isMaintenance()) {
                            this.updateBukkitSign(bukkitSign, current);
                            return;
                        }

                        SignAnimation loadingLayout = CloudSignAPI.getInstance().getSignConfiguration().getLoadingLayout();
                        SignLayout signLayout;
                        if (animationsTick >= CloudSignAPI.getInstance().getSignConfiguration().getLoadingLayout().size()) {
                            animationsTick = 0;
                            signLayout = loadingLayout.get(0);
                        } else {
                            signLayout = loadingLayout.get(animationsTick);
                        }
                        for (int i = 0; i != 4; i++) {
                            bukkitSign.setLine(i, ChatColor.translateAlternateColorCodes('&', current.getTask().replacePlaceHolders(signLayout.getLines()[i])));
                        }
                        bukkitSign.update(true);
                        Bukkit.getScheduler().runTask(BukkitBootstrap.getInstance(), () -> this.setBlock(signLayout, bukkitSign.getLocation(), null));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                if (cloudSign != null) {
                    try {
                        Location bukkitLocation = new Location(Bukkit.getWorld(cloudSign.getLocation().getWorld()), cloudSign.getLocation().getX(), cloudSign.getLocation().getY(), cloudSign.getLocation().getZ());

                        if (!bukkitLocation.getWorld().getName().equalsIgnoreCase(cloudSign.getLocation().getWorld())) {
                            return;
                        }

                        Block blockAt = Bukkit.getServer().getWorld(cloudSign.getLocation().getWorld()).getBlockAt(bukkitLocation);
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
    public Collection<ICloudSign> getOfflineSigns(String name) {

        Set<Integer> allSigns = new BukkitCloudSignGroup(name, CloudSignAPI.getInstance().getSignManager().getAllCachedCloudSigns()).getCloudSigns().keySet();
        Set<Integer> onlineSigns = freeSigns.get(name).keySet();

        if (onlineSigns.size() == allSigns.size()) {
            return new LinkedList<>();
        } else {
            for (Integer onlineSign : onlineSigns) {
                allSigns.remove(onlineSign);
            }

            Collection<ICloudSign> offlineSigns = new ArrayList<>();
            for (Integer count : allSigns) {
                ICloudSign sign = new BukkitCloudSignGroup(name, CloudSignAPI.getInstance().getSignManager().getAllCachedCloudSigns()).getCloudSigns().get(count);
                ICloudServer s = CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(sign.getTaskName() + "-" + count);
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
            signLayout = CloudSignAPI.getInstance().getSignConfiguration().getMaintenanceLayout();
            state = service.getServiceState();
        } else if (service.getServiceState().equals(ServiceState.STARTING)) {
            signLayout = CloudSignAPI.getInstance().getSignConfiguration().getStartingLayOut();
            state = ServiceState.STARTING;
        } else if (service.getOnlinePlayerCount() >= service.getMaxPlayers()) {
            signLayout = CloudSignAPI.getInstance().getSignConfiguration().getFullLayout();
            state = service.getServiceState();
        } else {
            signLayout = CloudSignAPI.getInstance().getSignConfiguration().getOnlineLayout();
            state = ServiceState.ONLINE;
        }

        //Updating sign line
        for (int i = 0; i != 4; i++) {
            sign.setLine(i, ChatColor.translateAlternateColorCodes('&', service.replacePlaceHolders(signLayout.getLines()[i])));
        }
        sign.update(true);
        Bukkit.getScheduler().runTask(BukkitBootstrap.getInstance(), () ->  this.setBlock(signLayout, sign.getLocation(), state));
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
                block.setData((byte) 14);
                e.printStackTrace();
            }
        }
    }


    /**
     * Filters for a {@link UniversalCloudSign} by bukkit-location
     *
     * @param location the location
     * @return sign or null
     */
    public ICloudSign getCloudSign(Location location) {
        return CloudSignAPI.getInstance()
                .getSignManager()
                .getAllCachedCloudSigns()
                .stream()
                .filter(cloudSign -> cloudSign.getLocation().getX() == location.getBlockX())
                .filter(cloudSign -> cloudSign.getLocation().getY() == location.getBlockY())
                .filter(cloudSign -> cloudSign.getLocation().getZ() == location.getBlockZ())
                .filter(cloudSign -> cloudSign.getLocation().getWorld().equalsIgnoreCase(location.getWorld().getName()))
                .findFirst()
                .orElse(null);
    }
}