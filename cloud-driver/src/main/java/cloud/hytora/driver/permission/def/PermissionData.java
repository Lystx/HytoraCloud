package cloud.hytora.driver.permission.def;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PermissionData implements Bufferable {

    /**
     * All cached permissions of this data
     */
    private Collection<String> permissions;

    /**
     * All specific task permissions
     */
    private Collection<TaskPermissionData> taskPermissions;

    /**
     * All group data
     */
    private Collection<PlayerGroupData> groups;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buffer) throws IOException {
        switch (state) {
            case WRITE:
                buffer.writeStringCollection(permissions);
                buffer.writeObjectCollection(taskPermissions);
                buffer.writeObjectCollection(groups);
                break;

            case READ:
                permissions = buffer.readStringCollection();
                taskPermissions = buffer.readObjectCollection(TaskPermissionData.class);
                groups = buffer.readObjectCollection(PlayerGroupData.class);
                break;
        }
    }


    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class TaskPermissionData implements Bufferable {

        /**
         * The name of the data
         */
        private String name;

        /**
         * The task name
         */
        private String task;


        @Override
        public void applyBuffer(BufferState state, @NotNull PacketBuffer buffer) throws IOException {
            switch (state) {
                case WRITE:
                    buffer.writeString(name);
                    buffer.writeString(task);
                    break;

                case READ:
                    name = buffer.readString();
                    task = buffer.readString();
                    break;
            }
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class PlayerGroupData implements Bufferable {

        /**
         * The uuid of the group
         */
        private UUID uniqueId;

        /**
         * The timeout for this group
         */
        private long timeout;

        @Override
        public void applyBuffer(BufferState state, @NotNull PacketBuffer buffer) throws IOException {
            switch (state) {
                case WRITE:
                    buffer.writeUniqueId(uniqueId);
                    buffer.writeLong(timeout);
                    break;

                case READ:
                    uniqueId = buffer.readUniqueId();
                    timeout = buffer.readLong();
                    break;
            }
        }
    }

}
