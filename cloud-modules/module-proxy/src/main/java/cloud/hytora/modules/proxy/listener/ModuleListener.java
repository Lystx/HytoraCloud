package cloud.hytora.modules.proxy.listener;

import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerDisconnect;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerLoginSuccess;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUpdate;
import cloud.hytora.modules.proxy.ProxyModule;

public class ModuleListener {

    @EventListener
    public void handle(CloudEventServiceUpdate event) {
        ProxyModule.getInstance().update();
    }

    @EventListener
    public void handle(CloudEventPlayerLoginSuccess event) {
        ProxyModule.getInstance().update();
    }

    @EventListener
    public void handle(CloudEventPlayerDisconnect event) {
        ProxyModule.getInstance().update();
    }
}
