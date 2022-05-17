package cloud.hytora.driver.services.template.def;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
    public String buildTemplatePath() {
        return this.prefix + "/" + this.name;
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
