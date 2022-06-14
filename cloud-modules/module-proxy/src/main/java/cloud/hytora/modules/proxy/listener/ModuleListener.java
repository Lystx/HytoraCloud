package cloud.hytora.modules.proxy.listener;

import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;
import cloud.hytora.modules.proxy.ProxyModule;

public class ModuleListener {

    @EventListener
    public void handle(TaskUpdateEvent event) {
        ProxyModule.getInstance().updateTabList();
        ProxyModule.getInstance().updateMotd();
    }

    @EventListener
    public void handle(ServiceRegisterEvent event) {
        ProxyModule.getInstance().updateTabList();
        ProxyModule.getInstance().updateMotd();
    }

    @EventListener
    public void handle(ServiceUnregisterEvent event) {
        ProxyModule.getInstance().updateTabList();
        ProxyModule.getInstance().updateMotd();
    }
}
