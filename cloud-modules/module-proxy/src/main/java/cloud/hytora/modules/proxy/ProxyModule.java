package cloud.hytora.modules.proxy;

import cloud.hytora.common.collection.IRandom;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.modules.proxy.command.ProxyCommand;
import cloud.hytora.modules.proxy.config.*;
import cloud.hytora.modules.proxy.config.sub.MotdLayOut;
import cloud.hytora.modules.proxy.config.sub.TabListFrame;
import cloud.hytora.modules.proxy.listener.ModuleListener;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@ModuleConfiguration(
        name = "module-proxy",
        main = ProxyModule.class,
        author = "Lystx",
        description = "",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-notify",
        copyType = ModuleCopyType.NONE,
        environment = ModuleEnvironment.NODE
)
public class ProxyModule extends AbstractModule {

    private final Logger logger = Logger.newInstance();

    /**
     * The static module instance
     */
    @Getter
    private static ProxyModule instance;

    /**
     * The config to get data
     */
    private ProxyConfig proxyConfig;

    public ProxyModule(ModuleController controller) {
        super(controller);

        instance = this;
    }

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {

        loadConfig();
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {

        CloudDriver.getInstance().getEventManager().registerListener(new ModuleListener());
        CloudDriver.getInstance().getCommandManager().registerCommand(new ProxyCommand());

        //scheduling tab update
        Scheduler.runTimeScheduler().scheduleRepeatingTask(this::updateTabList, 0L, (long) (proxyConfig.getTablist().getAnimationInterval() * 1000));
    }

    @ModuleTask(id = 4, state = ModuleState.API_UPDATE)
    public void update() {
        updateTabList();
        updateMotd();
    }

    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void disable() {
        CloudDriver.getInstance().getEventManager().unregisterListener(ModuleListener.class);
        CloudDriver.getInstance().getCommandManager().unregisterCommand(ProxyCommand.class);
    }


    protected int tablistAnimationIndex = 0;

    public String[] selectTabList() {

        try {
            if (proxyConfig.getTablist().getFrames().size() >= tablistAnimationIndex++) {
                tablistAnimationIndex = 0;
            }
            TabListFrame frame = proxyConfig.getTablist().getFrames().get(tablistAnimationIndex);
            String header = frame.getHeader();
            String footer = frame.getFooter();

            header = header.replaceAll("&", "§");
            footer = footer.replaceAll("&", "§");

            return new String[]{header, footer};

        } catch (Exception e) {
            return new String[]{"", ""};
        }
    }

    public void updateTabList() {
        String[] tabList = selectTabList();
        String[] header = {tabList[0]};
        String[] footer = {tabList[1]};

        //setting tabList
        for (ICloudPlayer cloudPlayer : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            ICloudService proxyServer = cloudPlayer.getProxyServer();

            PlayerExecutor executor = PlayerExecutor.forPlayer(cloudPlayer);
            ICloudService server = cloudPlayer.getServer();

            if (server != null) {
                header[0] = server.replacePlaceHolders(header[0]);
                footer[0] = server.replacePlaceHolders(footer[0]);
            }

            //proxy place holder
            header[0] = header[0].replace("{proxy}", proxyServer.getName());
            footer[0] = footer[0].replace("{proxy}", proxyServer.getName());

            header[0] = header[0].replace("{service}", (cloudPlayer.getServer() == null ? "UNKNOWN" : cloudPlayer.getServer().getName()));
            footer[0] = footer[0].replace("{service}", (cloudPlayer.getServer() == null ? "UNKNOWN" : cloudPlayer.getServer().getName()));

            //player placeholder
            header[0] = header[0].replace("{players.online}", "" + CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers().size());
            footer[0] = footer[0].replace("{players.online}", "" + CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers().size());
            footer[0] = footer[0].replace("{players.max}", "" + CloudDriver.getInstance().getPlayerManager().countPlayerCapacity());
            header[0] = header[0].replace("{players.max}", "" + CloudDriver.getInstance().getPlayerManager().countPlayerCapacity());

            executor.setTabList(header[0], footer[0]);
        }
    }

    public void updateMotd() {
        for (IServiceTask serviceTask : CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().stream().filter(t -> t.getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY).collect(Collectors.toList())) {

            MotdLayOut motd = selectMotd(serviceTask);
            if (motd == null) {
                continue;
            }
            for (ICloudService service : CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.PROXY)) {
                service.editPingProperties(ping -> {
                    ping.setMotd(replaceDefault(service, (motd.getFirstLine() + "\n" + motd.getSecondLine())));
                    ping.setVersionText(replaceDefault(service, motd.getProtocolText()));
                    ping.setPlayerInfo(motd.getPlayerInfo().toArray(new String[0]));
                    ping.setCombineAllProxiesIfProxyService(true);
                    ping.setUsePlayerPropertiesOfService(true);
                });
            }
        }
    }

    protected String replaceDefault(ICloudService info, String content) {
        if (content == null) {
            return "";
        }
        content = content.replaceAll("&", "§");

        int maxPlayers = -1;
        return content
                .replace("{proxy}", info.getName())
                .replace("{node}", info.getRunningNodeName())
                .replace("{players.online}", CloudDriver.getInstance().getPlayerManager().getCloudPlayerOnlineAmount() + "")
                .replace("{players.max}", maxPlayers + "")
                ;
    }

    @Nullable
    public MotdLayOut selectMotd(IServiceTask task) {
        List<MotdLayOut> elements = task.isMaintenance() ? proxyConfig.getMotd().getMaintenances() : proxyConfig.getMotd().getDefaults();
        if (elements.isEmpty()) {
            return null;
        }
        return IRandom.singleton().choose(elements);
    }


    public void loadConfig() {
        logger.debug("Loading proxy config...");
        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(proxyConfig = ProxyConfig.defaultConfig());
            controller.getConfig().save();
        } else {
            proxyConfig = controller.getConfig().toInstance(ProxyConfig.class);
        }
        logger.debug("Loaded config {}", proxyConfig);
    }
}
