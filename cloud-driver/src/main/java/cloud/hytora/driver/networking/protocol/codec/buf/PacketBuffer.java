package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.INetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

// TODO: 01.03.2022 documentation
public interface PacketBuffer {


	static PacketBuffer unPooled() {
		return unPooled(CloudDriver.getInstance().getExecutor());
	}

	@Deprecated
	static PacketBuffer unsafe() {
		try {
			return unPooled();
		} catch (Exception e) {
			return unPooled(null);
		}
	}

	static PacketBuffer unPooled(INetworkExecutor participant) {
		return new DefaultPacketBuffer(Unpooled.buffer(), participant);
	}

	ByteBuf nettyBuffer();

	/**
	 * @return the amount of total bytes
	 */
	int length();

	/**
	 * @return the amount of remaining readable bytes
	 */
	int remaining();

	boolean remain(int amount);

	@Nonnull
	byte[] asArray();

	void read(@Nonnull byte[] bytes);

	void writeFile(File file) throws IOException;

	File readFile(File destinationFile) throws IOException;

	File readFile() throws IOException;

	void read(@Nonnull OutputStream out, int length) throws IOException;

	<T extends IPacket> T readPacket() throws IOException;

	PacketBuffer writePacket(IPacket packet) throws IOException;

	@Nonnull
	PacketBuffer write(@Nonnull byte[] bytes);

	ProtocolAddress readAddress();

	PacketBuffer writeAddress(@Nonnull ProtocolAddress address);

	@Nonnull
	PacketBuffer write(@Nonnull byte[] bytes, int index, int length);

	boolean readBoolean();

	@Nonnull
	PacketBuffer writeByte(byte value);

	byte readByte();

	@Nonnull
	PacketBuffer writeBoolean(boolean value);

	int readInt();

	@Nonnull
	PacketBuffer writeInt(int value);

	int readVarInt();

	@Nonnull
	PacketBuffer writeVarInt(int value);

	long readLong();

	@Nonnull
	PacketBuffer writeLong(long value);

	long readVarLong();

	@Nonnull
	PacketBuffer writeVarLong(long value);

	float readFloat();

	@Nonnull
	PacketBuffer writeFloat(float value);

	double readDouble();

	@Nonnull
	PacketBuffer writeDouble(double value);

	char readChar();

	@Nonnull
	PacketBuffer writeChar(char value);

	@Nonnull
	byte[] readArray();

	@Nonnull
	PacketBuffer writeArray(@Nonnull byte[] array);

	@Nullable
	byte[] readOptionalArray();

	@Nonnull
	PacketBuffer writeOptionalArray(@Nullable byte[] array);

	@Nonnull
	String readString();

	@Nonnull
	PacketBuffer writeString(@Nonnull String string);

	@Nullable
	String readOptionalString();

	@Nonnull
	PacketBuffer writeOptionalString(@Nullable String string);

	@Nonnull
	List<String> readStringCollection();

	@Nonnull
	PacketBuffer writeStringCollection(@Nonnull Collection<? extends String> strings);

	@Nonnull
	String[] readStringArray();

	@Nullable
	String[] readOptionalStringArray();

	@Nonnull
	PacketBuffer writeStringArray(@Nonnull String[] strings);

	@Nonnull
	PacketBuffer writeOptionalStringArray(@Nullable String[] strings);

	@Nonnull
	UUID readUniqueId();

	default PacketBuffer append(@Nullable Consumer<? super PacketBuffer> handler) {
		if (handler == null) {
			return this;
		}
		handler.accept(this);
		return this;
	}

	@Nonnull
	PacketBuffer writeUniqueId(@Nonnull UUID uniqueId);

	@Nullable
	UUID readOptionalUniqueId();

	@Nonnull
	PacketBuffer writeOptionalUniqueId(@Nullable UUID uniqueId);

	@Nonnull
	List<UUID> readUniqueIdCollection();

	@Nonnull
	PacketBuffer writeUniqueIdCollection(@Nonnull Collection<? extends UUID> uniqueIds);

	@Nonnull
	PacketBuffer writeInetAddress(@Nonnull InetAddress address);

	@Nonnull
	InetAddress readInetAddress();

	@Nonnull
	Document readDocument();

	@Nonnull
	PacketBuffer writeDocument(@Nonnull Document document);

	@Nullable
	Document readOptionalDocument();

	@Nonnull
	PacketBuffer writeOptionalDocument(@Nullable Document document);

	@Nonnull
	List<Document> readDocumentCollection();

	@Nonnull
	PacketBuffer writeDocumentCollection(@Nonnull Collection<? extends Document> documents);

	@Nonnull
	Document[] readDocumentArray();

	@Nonnull
	PacketBuffer writeDocumentArray(@Nonnull Document[] documents);

	@Nonnull
	<T extends IBufferObject> T readObject(@Nonnull Class<T> objectClass);

	@Nonnull
	PacketBuffer writeObject(@Nonnull IBufferObject object);

	@Nullable
	<T extends IBufferObject> T readOptionalObject(@Nonnull Class<T> objectClass);

	@Nonnull
	PacketBuffer writeOptionalObject(@Nullable IBufferObject object);

	@Nonnull
	<T extends IBufferObject> Collection<T> readObjectCollection(@Nonnull Class<T> objectClass);

	@Nonnull
	<T extends IBufferObject, W extends T> Collection<T> readWrapperObjectCollection(Class<W> objectClass);

	@Nonnull
	PacketBuffer writeObjectCollection(@Nonnull Collection<? extends IBufferObject> objects);

	@Nonnull
	<T extends IBufferObject> T[] readObjectArray(@Nonnull Class<T> objectClass);

	@Nonnull
	<T extends IBufferObject> PacketBuffer writeObjectArray(@Nonnull T[] objects);

	@Nonnull
	<E extends Enum<?>> E readEnum(@Nonnull Class<E> enumClass);

	@Nonnull
	PacketBuffer writeEnum(@Nonnull Enum<?> value);

	@Nullable
	<E extends Enum<?>> E readOptionalEnum(@Nonnull Class<E> enumClass);

	@Nonnull
	PacketBuffer writeOptionalEnum(@Nonnull Enum<?> value);

	@Nonnull
	<E extends Enum<?>> List<E> readEnumCollection(@Nonnull Class<E> enumClass);

	@Nonnull
	PacketBuffer writeEnumCollection(@Nonnull Collection<? extends Enum<?>> enums);

	@Nonnull
	Throwable readThrowable();

	@Nonnull
	PacketBuffer writeThrowable(@Nonnull Throwable value);

	@Nonnull
	PacketBuffer release();

	@Nonnull
	PacketBuffer copy();

	@Nonnull
	<T> List<T> readCollection(@Nonnull Supplier<T> reader);

	@Nonnull
	<T> PacketBuffer writeCollection(@Nonnull Collection<? extends T> collection, @Nonnull Consumer<T> writer);

	@Nonnull
	<T> T[] readArray(@Nonnull Class<T> theClass, @Nonnull Supplier<T> reader);

	@Nonnull
	<T> PacketBuffer writeArray(@Nonnull T[] array, @Nonnull Consumer<T> writer);

	@Nullable
	<T> T readOptional(@Nonnull Supplier<T> reader);

	@Nonnull
	<T> PacketBuffer writeOptional(@Nullable T object, @Nonnull Consumer<T> writer);

	PacketBuffer readBuffer();

	void writeBuffer(@Nonnull PacketBuffer buffer);

}
