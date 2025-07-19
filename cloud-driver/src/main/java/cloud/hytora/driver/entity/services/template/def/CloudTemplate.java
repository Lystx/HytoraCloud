package cloud.hytora.driver.entity.services.template.def;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.TemplateStorage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CloudTemplate implements ServiceTemplate {

    private String name;
    private String prefix;
    private String storageName;
    private boolean copyToStatic;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeString(prefix);
                buf.writeString(name);
                buf.writeString(storageName);
                buf.writeBoolean(copyToStatic);
                break;
            case READ:
                prefix = buf.readString();
                name = buf.readString();
                storageName = buf.readString();
                copyToStatic = buf.readBoolean();
                break;
        }
    }

    @Override
    public Task<Collection<File>> getDirectoryContentsAsync(String dirName) {
        return CloudDriver.getInstance().getTemplateManager().getDirectoryContentsAsync(this, dirName);
    }

    @Override
    public String buildTemplatePath() {
        return this.name + "/" + this.prefix;
    }

    @Override
    public TemplateStorage getStorage() {
        return CloudDriver.getInstance().getTemplateManager().getStorage(this.storageName);
    }

    @Override
    public boolean shouldCopyToStatic() {
        return copyToStatic;
    }
}
