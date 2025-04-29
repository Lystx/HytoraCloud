package cloud.hytora.modules.proxy.listener;

import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.player.CloudPlayerChangeServerEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerDisconnectEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginSuccessEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUpdateEvent;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;
import cloud.hytora.modules.proxy.ProxyModule;

public class ModuleListener {

    @EventListener
    public void handle(ServiceUpdateEvent event) {
        ProxyModule.getInstance().update();
    }

    @EventListener
    public void handle(CloudPlayerLoginSuccessEvent event) {
        ProxyModule.getInstance().update();
    }

    @EventListener
    public void handle(CloudPlayerDisconnectEvent event) {
        ProxyModule.getInstance().update();
    }
}
