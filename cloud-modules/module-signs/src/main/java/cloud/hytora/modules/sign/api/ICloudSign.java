package cloud.hytora.modules.sign.api;

import cloud.hytora.common.identification.ImmutableUUIDHolder;
import cloud.hytora.common.location.ModifiableLocation;
import cloud.hytora.common.task.ITask;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.services.task.IServiceTask;

import java.util.UUID;

public interface ICloudSign extends ImmutableUUIDHolder, IBufferObject {

    UUID getUniqueId();

    String getTaskName();

    ITask<IServiceTask> findTaskAsync();

    IServiceTask findTask();

    ModifiableLocation<Integer> getLocation();
}
