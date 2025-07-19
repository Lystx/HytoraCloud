package cloud.hytora.remote.impl;


import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.defaults.task.CloudEventTaskUpdate;
import cloud.hytora.driver.entity.services.task.DefaultServiceTaskManager;

import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.networking.packets.entities.PacketServiceTask;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class RemoteServiceTaskManager extends DefaultServiceTaskManager {

    @Override
    public void deployFile(CloudService task, File file, ServiceTemplate template, String destinationPathInTemplate) {
        PacketServiceTask.deployFile(task, file.getPath(), template, destinationPathInTemplate).publish();
    }

    @Override
    public void update(@NotNull ServiceTask task) {
        Remote.getInstance().getEventManager().callEvent(new CloudEventTaskUpdate(task), PublishingType.PROTOCOL);
    }


    @EventListener
    public void handleUpdate(CloudEventTaskUpdate event) {
        ServiceTask packetTask = event.getTask();
        ServiceTask task = getCachedServiceTask(packetTask.getName());
        if (task == null) {
            return;
        }

        this.allCachedTasks.remove(task);
        task.clone(packetTask);
        this.allCachedTasks.add(task);
    }

}
