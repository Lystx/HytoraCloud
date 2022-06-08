package cloud.hytora.remote.impl;


import cloud.hytora.driver.services.task.DefaultServiceTaskManager;
import cloud.hytora.driver.networking.packets.group.ServerConfigurationCacheUpdatePacket;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.remote.Remote;
import org.jetbrains.annotations.NotNull;

public class RemoteServiceTaskManager extends DefaultServiceTaskManager {

    @Override
    public void update(@NotNull ServiceTask task) {
        Remote.getInstance().getClient().sendPacket(new ServerConfigurationCacheUpdatePacket(task));
    }

}
