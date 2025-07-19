package cloud.hytora.driver.common.objects;

import cloud.hytora.common.identification.ImmutableNameHolder;
import cloud.hytora.driver.networking.PacketSender;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface NetworkEntity<T> extends PacketSender, ImmutableNameHolder, Cloneable<T>, Identifiable, PlaceHolder, IBufferObject {

}
