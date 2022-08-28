package cloud.hytora.remote.impl;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;
import cloud.hytora.driver.services.task.DefaultServiceTaskManager;

import cloud.hytora.driver.services.task.IServiceTask;
import org.jetbrains.annotations.NotNull;

public class RemoteServiceTaskManager extends DefaultServiceTaskManager {

    @Override
    public void updateTask(@NotNull IServiceTask task) {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new TaskUpdateEvent(task));
    }


    @EventListener
    public void handleUpdate(TaskUpdateEvent event) {
        IServiceTask packetTask = event.getTask();
        IServiceTask task = getTaskOrNull(packetTask.getName());
        if (task != null) {
            task.copy(packetTask);
        }
    }

}
