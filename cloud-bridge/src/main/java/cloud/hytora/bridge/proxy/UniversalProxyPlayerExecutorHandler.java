package cloud.hytora.bridge.proxy;

import cloud.hytora.bridge.CloudBridge;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.driver.DriverCacheUpdateEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.task.TaskMaintenanceChangeEvent;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import cloud.hytora.remote.adapter.IBridgeProxyExtension;
import net.md_5.bungee.api.ProxyServer;

import java.util.List;

public class UniversalProxyPlayerExecutorHandler {

    public static void init() {
        UniversalProxyPlayerExecutorHandler handler = new UniversalProxyPlayerExecutorHandler();

        handler.preCacheServices();
        CloudDriver
                .getInstance()
                .getProviderRegistry()
                .get(IEventManager.class)
                .ifPresent(eventManager -> {
                    eventManager.registerListener(handler);
                });
    }

    private UniversalProxyPlayerExecutorHandler() {
    }



    /**
     * Pre caches every loaded {@link ICloudServer}
     * before this plugin is even loaded and ready to use
     */
    private void preCacheServices() {
        CloudDriver
                .getInstance()
                .getProviderRegistry()
                .get(ICloudServiceManager.class)
                .ifPresent(sm -> {
                    sm.getAllCachedServices()
                            .stream()
                            .filter(t -> t.getTask().getVersion().isProxy())
                            .forEach(s -> {
                                IBridgeProxyExtension adapter = CloudBridge.getRemoteExtension().asProxyExtension();
                                if (adapter != null) {
                                    adapter.unregisterService(s);
                                }
                            });
                });
    }

    @EventListener
    public void handle(ServiceRegisterEvent event) {
        ICloudServer cloudServer = event.getCloudServer();
        if (cloudServer.getTask() == null) {
            return;
        }
        if (!cloudServer.getTask().getVersion().isProxy()) {
            IBridgeProxyExtension adapter = CloudBridge.getRemoteExtension().asProxyExtension();
            if (adapter != null) {
                adapter.registerService(cloudServer);
            }
        }
    }

    @EventListener
    public void handle(ServiceUnregisterEvent event) {
        IBridgeProxyExtension adapter = CloudBridge.getRemoteExtension().asProxyExtension();
        if (adapter != null) {
            adapter.registerService(event.getCloudServer());
        }
    }

    @EventListener
    public void handle(DriverCacheUpdateEvent event) {
        ProxyServer.getInstance().getServers().clear();
        for (ICloudServer service : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllCachedServices()) {
            if (!service.getTask().getVersion().isProxy()) {
                IBridgeProxyExtension adapter = CloudBridge.getRemoteExtension().asProxyExtension();
                if (adapter != null) {
                    adapter.registerService(service);
                }
            }
        }
    }


    //LISTENERS

    @EventListener
    public void handle(TaskMaintenanceChangeEvent event) {
        IServiceTask task = event.getTask();
        CloudMessages cloudMessages = CloudMessages.retrieveFromStorage();

        ICloudServer thisService = CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ICloudServiceManager.class)
                .thisServiceOrNull();

        if (event.isNewMaintenanceValue() && task.getName().equalsIgnoreCase(thisService.getTask().getName())) {

            List<String> whitelistedPlayers = CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class).getBundle("cloud::whitelist").toInstances(String.class);
            for (ICloudPlayer cp : thisService.getOnlinePlayers()) {
                if (whitelistedPlayers.contains(cp.getName())) {
                    PlayerExecutor.forPlayer(cp).sendMessage(cloudMessages.getMaintenanceKickByPassedMessage());
                    continue;
                }
                PlayerExecutor.forPlayer(cp).disconnect(cloudMessages.getNetworkCurrentlyInMaintenance());
            }

        }
    }
}
