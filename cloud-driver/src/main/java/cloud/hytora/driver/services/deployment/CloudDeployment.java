package cloud.hytora.driver.services.deployment;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.template.ITemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CloudDeployment implements IDeployment {

    private ITemplate template;
    private Collection<String> exclusionFiles;

    private Collection<String> onlyIncludedFiles;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeObject(this.template);
                buf.writeStringCollection(this.exclusionFiles);
                buf.writeStringCollection(this.onlyIncludedFiles);
                break;

            case READ:
                this.template = buf.readObject(ITemplate.class);
                this.exclusionFiles = buf.readStringCollection();
                this.onlyIncludedFiles = buf.readStringCollection();
                break;
        }
    }
}
