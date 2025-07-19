package cloud.hytora.driver.networking.packets.other;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.event.NetworkEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PacketCallEvent extends AbstractPacket {

    private NetworkEvent event;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeString(event.getClass().getName());
                buf.writeBoolean(CloudDriver.getInstance().getEnvironment() == CloudDriver.Environment.SERVICE);
                event.applyBuffer(BufferState.WRITE, buf);
                break;

            case READ:
                String className = buf.readString();
                boolean allowProtocolCall = buf.readBoolean();
                CloudDriver.getInstance().getLogger().debug("Received ProtocolTransferableEvent [{}]", className);
                try {
                    Class<? extends NetworkEvent> eventClass = (Class<? extends NetworkEvent>) Class.forName(className);
                    NetworkEvent event = ReflectionUtils.createEmpty(eventClass);
                    if (event != null) {
                        event.applyBuffer(BufferState.READ, buf);
                        if (allowProtocolCall) {
                            CloudDriver.getInstance().getEventManager().callEvent(event, PublishingType.GLOBAL); //make sure to prevent packet-transfer cycle
                        } else {
                            CloudDriver.getInstance().getEventManager().callEvent(event, PublishingType.INTERNAL); //make sure to prevent packet-transfer cycle
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
