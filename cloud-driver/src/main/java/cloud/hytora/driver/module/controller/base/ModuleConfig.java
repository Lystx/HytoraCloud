package cloud.hytora.driver.module.controller.base;


import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ModuleConfig implements Bufferable {

    private String name, author, description, version, mainClass, website;
    private String[] depends;
    private ModuleCopyType copyType;
    private ModuleEnvironment environment;

    @Nonnull
    public String getFullName() {
        return name + " v" + version + " by " + author;
    }

    @Override
    public String toString() {
        return "ModuleConfig[" + name + " v" + version + " by " + author + " copy=" + copyType + " environment=" + environment + "]";
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buffer) throws IOException {
        switch (state) {
            case READ:
                name = buffer.readString();
                author = buffer.readString();
                description = buffer.readString();
                version = buffer.readString();
                mainClass = buffer.readString();
                website = buffer.readOptionalString();
                depends = buffer.readStringArray();
                copyType = buffer.readEnum(ModuleCopyType.class);
                environment = buffer.readEnum(ModuleEnvironment.class);
                break;

            case WRITE:
                buffer.writeString(name);
                buffer.writeString(author);
                buffer.writeString(description);
                buffer.writeString(version);
                buffer.writeString(mainClass);
                buffer.writeOptionalString(website);
                buffer.writeStringArray(depends);
                buffer.writeEnum(copyType);
                buffer.writeEnum(environment);
                break;
        }
    }
}
