package cloud.hytora.modules.proxy;

import cloud.hytora.common.collection.IRandom;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.context.annotations.Constructor;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
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

public class ProxyModule {

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

    private final ModuleController controller;

    public ProxyModule(ModuleController controller) {
        this.controller = controller;
    }

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {
        instance = this;

        loadConfig();
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {

        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(new ModuleListener());
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).registerCommands(new ProxyCommand());

        //scheduling tab updateTask
        Scheduler.runTimeScheduler().scheduleRepeatingTask(this::updateTabList, 0L, (long) (proxyConfig.getTablist().getAnimationInterval() * 1000));
    }

    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void disable() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).unregisterListener(ModuleListener.class);
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).unregister(ProxyCommand.class);
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
        final String[] header = {tabList[0]};
        final String[] footer = {tabList[1]};

        //setting tabList
        for (ICloudPlayer cloudPlayer : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getAllCachedCloudPlayers()) {
            cloudPlayer.getProxyServerAsync().onTaskSucess(proxyServer -> {

                PlayerExecutor executor = PlayerExecutor.forPlayer(cloudPlayer);
                ICloudServer server = cloudPlayer.getServer();

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
                header[0] = header[0].replace("{players.online}", "" + CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getAllCachedCloudPlayers().size());
                footer[0] = footer[0].replace("{players.online}", "" + CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getAllCachedCloudPlayers().size());
                footer[0] = footer[0].replace("{players.max}", "" + CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).countPlayerCapacity());
                header[0] = header[0].replace("{players.max}", "" + CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).countPlayerCapacity());

                executor.setTabList(header[0], footer[0]);
            });
        }

    }

    public void updateMotd() {
        for (IServiceTask serviceTask : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getAllCachedTasks().stream().filter(t -> t.getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY).collect(Collectors.toList())) {

            MotdLayOut motd = selectMotd(serviceTask);
            if (motd == null) {
                continue;
            }
            for (ICloudServer cloudServer : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllServicesByEnvironment(SpecificDriverEnvironment.PROXY)) {

                cloudServer.editPingProperties(ping -> {
                    ping.setMotd(replaceDefault(cloudServer, (motd.getFirstLine() + "\n" + motd.getSecondLine())));
                    ping.setVersionText(replaceDefault(cloudServer, motd.getProtocolText()));
                    ping.setPlayerInfo(motd.getPlayerInfo().toArray(new String[0]));
                    ping.setCombineAllProxiesIfProxyService(true);
                    ping.setUsePlayerPropertiesOfService(true);
                });
            }
        }
    }

    protected String replaceDefault(ICloudServer info, String content) {
        if (content == null) {
            return "";
        }
        content = content.replaceAll("&", "§");

        int maxPlayers = -1;
        return content
                .replace("{proxy}", info.getName())
                .replace("{node}", info.getRunningNodeName())
                .replace("{players.online}", CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getCloudPlayerOnlineAmount() + "")
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
        if (controller.getConfig().isEmpty()) {
            controller.getConfig().set(proxyConfig = ProxyConfig.defaultConfig());
            controller.getConfig().save();
        } else {
            proxyConfig = controller.getConfig().toInstance(ProxyConfig.class);
        }
    }
}
