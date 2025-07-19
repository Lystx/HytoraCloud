package cloud.hytora.node.service;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceReady;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceRegistered;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUnregistered;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.language.TranslatedEntry;
import cloud.hytora.driver.language.Translation;
import cloud.hytora.node.NodeDriver;

public class InternalNotifyListener {


    @EventListener
    public void handleAdd(CloudEventServiceRegistered event) {
        if (!CloudDriver.getInstance().isRunning()) {
            return;
        }
        CloudService cloudService = event.getCloudServer();

        Translation.entryOf("service.notify.register").applyPlaceHolders(cloudService).print();
    }


    @EventListener
    public void handleRemove(CloudEventServiceUnregistered event) {
        if (!CloudDriver.getInstance().isRunning()) {
            return;
        }
        CloudService cloudServer = event.getCloudServer();
        Translation.entryOf("service.notify.unregister").applyPlaceHolders(cloudServer).print();
    }

    @EventListener
    public void handleReady(CloudEventServiceReady event) {
        if (!CloudDriver.getInstance().isRunning()) {
            return;
        }
        CloudService cloudServer = event.getCloudServer();
        Translation.entryOf("service.notify.ready").replace("bootup.time", NodeDriver.getInstance().getExecutor().getStats(cloudServer)).applyPlaceHolders(cloudServer).print();
    }
}
