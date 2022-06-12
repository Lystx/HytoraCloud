package cloud.hytora.remote.impl;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;
import cloud.hytora.driver.services.task.DefaultServiceTaskManager;

import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

public class RemoteServiceTaskManager extends DefaultServiceTaskManager {

    @Override
    public void update(@NotNull ServiceTask task) {
        Remote.getInstance().getEventManager().callEventGlobally(new TaskUpdateEvent(task));
    }


    @EventListener
    public void handleUpdate(TaskUpdateEvent event) {
        ServiceTask packetTask = event.getTask();
        ServiceTask task = getTaskByNameOrNull(packetTask.getName());
        task.cloneInternally(packetTask, task);
    }

}
