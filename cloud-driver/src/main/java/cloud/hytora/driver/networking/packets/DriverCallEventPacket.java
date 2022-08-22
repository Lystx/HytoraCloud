package cloud.hytora.driver.networking.packets;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.DriverEnvironment;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.ProtocolTansferableEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DriverCallEventPacket extends AbstractPacket {

    private ProtocolTansferableEvent event;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeString(event.getClass().getName());
                buf.writeBoolean(CloudDriver.getInstance().getEnvironment() == DriverEnvironment.SERVICE);
                event.applyBuffer(BufferState.WRITE, buf);
                break;

            case READ:
                String className = buf.readString();
                boolean allowProtocolCall = buf.readBoolean();
                CloudDriver.getInstance().getLogger().debug("Received ProtocolTransferableEvent [{}]", className);
                try {
                    Class<? extends ProtocolTansferableEvent> eventClass = (Class<? extends ProtocolTansferableEvent>) Class.forName(className);
                    ProtocolTansferableEvent event = ReflectionUtils.createEmpty(eventClass);
                    if (event != null) {
                        event.applyBuffer(BufferState.READ, buf);
                        if (allowProtocolCall) {
                            CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(event); //make sure to prevent packet-transfer cycle
                        } else {
                            CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventOnlyLocally(event); //make sure to prevent packet-transfer cycle
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
