package cloud.hytora.modules.proxy;

import cloud.hytora.common.collection.IRandom;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.modules.proxy.command.ProxyCommand;
import cloud.hytora.modules.proxy.config.*;
import cloud.hytora.modules.proxy.listener.ModuleListener;
import cloud.hytora.remote.Remote;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ModuleConfiguration(
        name = "module-proxy",
        main = ProxyModule.class,
        author = "Lystx",
        description = "Manages the Proxy servers",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-proxy",
        copyType = ModuleCopyType.NONE,
        environment = ModuleEnvironment.NODE
)
public class ProxyModule extends DriverModule {


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

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {
        instance = this;

        loadConfig();
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {

        this.registerEvent(new ModuleListener());
        this.registerCommand(new ProxyCommand());

        //scheduling tab update
        Scheduler.runTimeScheduler().scheduleRepeatingTask(this::updateTabList, 0L, (long) (proxyConfig.getTablist().getAnimationInterval() * 1000));
    }

    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void disable() {

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
        String header = tabList[0];
        String footer = tabList[1];

        //setting tabList
        PlayerExecutor executor = PlayerExecutor.forAll();
        executor.setTabList(ChatComponent.text(header), ChatComponent.text(footer));

    }

    public void updateMotd() {
        for (ServiceTask serviceTask : CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().stream().filter(t -> t.getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY_SERVER).collect(Collectors.toList())) {

            MotdLayOut motd = selectMotd(serviceTask);
            if (motd == null) {
                continue;
            }
            for (ServiceInfo serviceInfo : CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.PROXY_SERVER)) {
                serviceInfo.editPingProperties(ping -> {
                    ping.setMotd(replaceDefault(serviceInfo, (motd.getFirstLine() + "\n" + motd.getSecondLine())));
                    ping.setVersionText(replaceDefault(serviceInfo, motd.getProtocolText()));
                    ping.setPlayerInfo(motd.getPlayerInfo().toArray(new String[0]));
                    ping.setCombineAllProxiesIfProxyService(true);
                    ping.setUsePlayerPropertiesOfService(true);
                });
            }
        }
    }

    protected String replaceDefault(ServiceInfo info, String content) {
        if (content == null) {
            return "";
        }
        content = content.replaceAll("&", "§");

        int maxPlayers = -1;
        return content
                .replace("{proxy}", info.getName())
                .replace("{node}", info.getTask().getNode())
                .replace("{players.online}", CloudDriver.getInstance().getPlayerManager().getCloudPlayerOnlineAmount() + "")
                .replace("{players.max}", maxPlayers + "")
                ;
    }

    @Nullable
    public MotdLayOut selectMotd(ServiceTask task) {
        List<MotdLayOut> elements = task.isMaintenance() ? proxyConfig.getMotd().getMaintenances() : proxyConfig.getMotd().getDefaults();
        if (elements.isEmpty()) {
            return null;
        }
        return IRandom.singleton().choose(elements);
    }


    public void loadConfig() {
        logger.info("Loading proxy config...");
        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(proxyConfig = ProxyConfig.defaultConfig());
            controller.getConfig().save();
        } else {
            proxyConfig = controller.getConfig().toInstance(ProxyConfig.class);
        }
        logger.info("Loaded config {}", proxyConfig);
    }
}
