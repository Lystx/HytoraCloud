package cloud.hytora.modules.proxy.listener;

import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheRegisterEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.event.defaults.task.TaskMaintenanceChangeEvent;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;
import cloud.hytora.modules.proxy.ProxyModule;

public class ModuleListener {

    @EventListener
    public void handle(TaskMaintenanceChangeEvent event) {
        ProxyModule.getInstance().updateTabList();
        ProxyModule.getInstance().updateMotd();
    }

    @EventListener
    public void handle(TaskUpdateEvent event) {
        ProxyModule.getInstance().updateTabList();
        ProxyModule.getInstance().updateMotd();
    }

    @EventListener
    public void handle(CloudServerCacheRegisterEvent event) {
        ProxyModule.getInstance().updateTabList();
        ProxyModule.getInstance().updateMotd();
    }

    @EventListener
    public void handle(CloudServerCacheUnregisterEvent event) {
        ProxyModule.getInstance().updateTabList();
        ProxyModule.getInstance().updateMotd();
    }
}
