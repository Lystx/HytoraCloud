package cloud.hytora.driver.services.task.bundle;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.task.TaskDownloadEntry;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.def.CloudTemplate;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultTaskGroup implements TaskGroup {

    private String name;
    private SpecificDriverEnvironment environment;
    private ServiceShutdownBehaviour shutdownBehaviour;

    private String[] javaArguments;
    private Collection<TaskDownloadEntry> downloadEntries;
    private Collection<CloudTemplate> templates;

    public Collection<ServiceTemplate> getTemplates() {
        return new ArrayList<>(templates);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeString(name);
                buf.writeEnum(environment);
                buf.writeEnum(shutdownBehaviour);

                buf.writeOptionalStringArray(javaArguments);
                buf.writeObjectCollection(downloadEntries);
                buf.writeObjectCollection(templates);
                break;

            case READ:
                name = buf.readString();
                environment = buf.readEnum(SpecificDriverEnvironment.class);
                shutdownBehaviour = buf.readEnum(ServiceShutdownBehaviour.class);
                javaArguments = buf.readOptionalStringArray();
                downloadEntries = buf.readObjectCollection(TaskDownloadEntry.class);
                templates = buf.readObjectCollection(CloudTemplate.class);

                break;
        }
    }

    @Override
    public Collection<IServiceTask> getChildren() {
        return CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks().stream().filter(c -> c.getTaskGroup().getName().equals(this.name)).collect(Collectors.toList());
    }
}
