package cloud.hytora.remote.impl;


import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.task.TaskUpdateEvent;
import cloud.hytora.driver.services.task.DefaultServiceTaskManager;

import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

public class RemoteServiceTaskManager extends DefaultServiceTaskManager {

    @Override
    public void update(@NotNull IServiceTask task) {
        Remote.getInstance().getEventManager().callEventGlobally(new TaskUpdateEvent(task));
    }


    @EventListener
    public void handleUpdate(TaskUpdateEvent event) {
        IServiceTask packetTask = event.getTask();
        IServiceTask task = getTaskByNameOrNull(packetTask.getName());
        task.clone(packetTask);
    }

}
