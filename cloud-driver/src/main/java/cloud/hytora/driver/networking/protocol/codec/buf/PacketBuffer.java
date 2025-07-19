package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.location.ModifiableLocation;
import cloud.hytora.common.location.impl.DefaultLocation;
import cloud.hytora.document.Document;
import cloud.hytora.driver.common.UniqueReturnValue;
import cloud.hytora.driver.common.http.impl.NettyUtils;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The {@link PacketBuffer} is an essential part of the cloud networking structure.
 * It is used to {@link BufferState#WRITE} and {@link BufferState#READ} certain data across the network
 * <br></br><br></br>
 *
 *
 * This PacketBuffer is used like a list of data.<br></br>
 * For example if you write the following into the PacketBuffer at some point:<br></br><br></br>
 *
 * 	  <b>-> packetbuffer.writeString("Hello World").writeInt(34).writeBoolean(true);</b><br></br><br></br>
 *
 * You have to read it the same way or else it will throw errors.<br></br>
 * For example:<br></br><br></br>
 *
 * 	   -> <b>String s = packetBuffer.readString();</b><br></br>
 * 	   -> <b>int i = packetBuffer.readInt();</b><br></br>
 * 	   -> <b>boolean b = packetBuffer.readBoolean();</b><br></br>
 * <br></br>
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface PacketBuffer {


	/**
	 * Creates a new {@link PacketBuffer} using the {@link NetworkExecutor}
	 * that is provided in the {@link CloudDriver}
	 *
	 * @see #unPooled(NetworkExecutor) for further detail
	 * @return PacketBuffer
	 */
	@Nonnull
	@UniqueReturnValue
	static PacketBuffer unPooled() {
		return unPooled(CloudDriver.getInstance().getExecutor());
	}

	static PacketBuffer unpooledWrapped(ByteBuf byteBuf, NetworkExecutor executor) {
		return new DefaultPacketBuffer(Unpooled.wrappedBuffer(NettyUtils.readByteArray(byteBuf, NettyUtils.readVarInt(byteBuf))), executor);
	}

	/**
	 * Creates a new {@link PacketBuffer} using the provided {@link NetworkExecutor}
	 * The executor is required to be set when writing {@link IPacket}s using this {@link PacketBuffer}
	 * Then the Packet needs to know who the sender instance is.
	 *
	 * @param participant the executor for the buffer
	 *
	 * @see NetworkExecutor
	 * @return the created buffer
	 */
	@Nonnull
	@UniqueReturnValue
	static PacketBuffer unPooled(NetworkExecutor participant) {
		return new DefaultPacketBuffer(Unpooled.buffer(), participant);
	}

	void writeThisObject(Object val);

	void writeThisObject(PacketBuffer buf, Object val);

	void readThisObject(PacketBuffer buf, Object val);
	void readThisObject(Object val);

	/**
	 * An unsafe method to get a {@link PacketBuffer}
	 * When any type of {@link Exception} occurs this method simply ignores it and
	 * nulls the {@link NetworkExecutor} which could lead to problems later on.
	 * Mostly used for testing purposes.
	 *
	 * @return the created buffer
	 */
	@Deprecated@Nonnull
	@UniqueReturnValue
	static PacketBuffer unsafe() {
		try {
			return unPooled();
		} catch (Exception e) {
			return unPooled(null);
		}
	}

	NetworkExecutor executor();

	/**
	 * @return the internal netty instance of buffer
	 */
	ByteBuf nettyBuffer();

	/**
	 * @return the amount of total bytes
	 */
	int length();

	/**
	 * @return the amount of remaining readable bytes
	 */
	int remaining();

	/**
	 * transforms this {@link PacketBuffer} to raw-data using method
	 * {@link #nettyBuffer()} and getting its raw bytes.
	 *
	 * @return the created data
	 */
	@Nonnull
	byte[] asArray();

	/**
	 * Raw method to interact with {@link #nettyBuffer()} and write bytes.
	 *
	 * @param bytes the bytes to send
	 * @return current buffer instance
	 */
	@Nonnull
	PacketBuffer write(@Nonnull byte[] bytes);
	PacketBuffer write(@Nonnull byte[] bytes, int index, int length);

	/**
	 * Reads the bytes from the {@link #nettyBuffer()}
	 *
	 * @param bytes the bytes to read
	 */
	void read(@Nonnull byte[] bytes);
	void read(@Nonnull OutputStream out, int length) throws IOException;

	/**
	 * Writes a {@link File} to this {@link PacketBuffer}
	 * This serializes the given {@link File} into bytes and
	 * puts them into the buffer.
	 * If the file does not exist or the directory in between the system
	 * will automatically create the directory or the file
	 *
	 * @param file the file to write
	 * @throws IOException if something went wrong
	 */
	PacketBuffer writeFile(File file) throws IOException;

	/**
	 * Reads a {@link File} from this {@link PacketBuffer}
	 * If the file does not exist or the directory in between the system
	 * will automatically create the directory or the file
	 *
	 * @param destinationFile the file where it should be
	 * @return the read file
	 */
	File readFile(File destinationFile);

	/**
	 * Reads a {@link Collection} of {@link File}s from this {@link PacketBuffer}
	 *
	 * @return the read collection
	 * @throws IOException if something went wrong
	 * @see #readFile()
	 */
	Collection<File> readFileCollection() throws IOException;

	Collection<File> readFileCollection(File directory) throws IOException;

	File readFileToParent(File parent);

	/**
	 * Writes a {@link Collection} of {@link File}s to this {@link PacketBuffer}
	 *
	 * @return the current buffer instance
	 * @throws IOException if something went wrong
	 * @see #writeFile(File) ()
	 */
	PacketBuffer writeFileCollection(Collection<File> files) throws IOException;


	File readFileAndOverride(File destinationFile) throws IOException;

	/**
	 * Unlike the other method, this does completely ignore the destination-file
	 * This method reads the same file path that was provided when writing it.
	 *
	 * @see #readFile(File)
	 * @return the read file
	 * @throws IOException if something went wrong
	 */
	@UniqueReturnValue
	File readFile() throws IOException;


	/**
	 * Reads an {@link IPacket} from this {@link PacketBuffer}
	 *
	 * @param <T> the generic type of the packet
	 * @return the read packet
	 * @throws IOException if something went wrong in {@link IPacket#applyBuffer(BufferState, PacketBuffer)}
	 */
	<T extends IPacket> T readPacket() throws IOException;

	/**
	 * Reads an {@link IPacket} from this {@link PacketBuffer}
	 *
	 * @param <T> the generic type of the packet
	 * @return the read packet
	 * @throws IOException if something went wrong in {@link IPacket#applyBuffer(BufferState, PacketBuffer)}
	 */
	<T extends IPacket> T readPacket(Class<T> packetClass) throws IOException;

	/**
	 * Writes an {@link IPacket} into this {@link PacketBuffer}
	 *
	 * @param packet the packet to write
	 * @return the current buffer instance
	 * @throws IOException if something went wrong in {@link IPacket#applyBuffer(BufferState, PacketBuffer)}
	 */
	PacketBuffer writePacket(@NotNull IPacket packet) throws IOException;

	/**
	 * Reads a {@link ProtocolAddress} from the current buffer
	 *
	 * @return the read instance
	 */
	ProtocolAddress readAddress();

	/**
	 * Writes the provided {@link ProtocolAddress} into this {@link PacketBuffer}
	 *
	 * @param address the address to write
	 * @return this buffer instance
	 */
	PacketBuffer writeAddress(@Nonnull ProtocolAddress address);

	/**
	 * Reads a {@link Boolean} from the current buffer
	 *
	 * @return the read instance
	 */
	boolean readBoolean();

	/**
	 * Writes the provided {@link Boolean} into this {@link PacketBuffer}
	 *
	 * @param value the boolean to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeBoolean(boolean value);

	/**
	 * Reads a {@link Integer} from the current buffer
	 *
	 * @return the read instance
	 */
	int readInt();

	/**
	 * Writes the provided {@link Integer} into this {@link PacketBuffer}
	 *
	 * @param value the integer to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeInt(int value);

	/**
	 * Reads a varInt from the current buffer
	 *
	 * @return the read instance
	 */
	int readVarInt();

	/**
	 * Writes the provided varInt into this {@link PacketBuffer}
	 *
	 * @param value the varInt to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeVarInt(int value);

	/**
	 * Reads a {@link Long} from the current buffer
	 *
	 * @return the read instance
	 */
	long readLong();

	/**
	 * Writes the provided {@link Long} into this {@link PacketBuffer}
	 *
	 * @param value the long to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeLong(long value);

	/**
	 * Reads a {@link Float} from the current buffer
	 *
	 * @return the read instance
	 */
	float readFloat();

	/**
	 * Writes the provided {@link Float} into this {@link PacketBuffer}
	 *
	 * @param value the float to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeFloat(float value);

	/**
	 * Reads a {@link Double} from the current buffer
	 *
	 * @return the read instance
	 */
	double readDouble();

	/**
	 * Writes the provided {@link Double} into this {@link PacketBuffer}
	 *
	 * @param value the value to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeDouble(double value);

	/**
	 * Reads a {@link Character} from the current buffer
	 *
	 * @return the read instance
	 */
	char readChar();

	/**
	 * Writes the provided {@link Character} into this {@link PacketBuffer}
	 *
	 * @param value the value to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeChar(char value);

	/**
	 * Reads a byte-array from the {@link #nettyBuffer()}
	 *
	 * @return the read instance
	 */
	@Nonnull
	byte[] readArray();

	/**
	 * Writes the provided byte-buffer into the wrapped {@link #nettyBuffer()}
	 *
	 * @param array the value to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeArray(@Nonnull byte[] array);

	/**
	 * Reads a {@link String} from the current buffer
	 *
	 * @return the read instance
	 */
	@Nonnull
	String readString();

	/**
	 * Reads a {@link Map} from this {@link PacketBuffer}
	 * It uses {@link BiSupplier} to read the keys and values of this map using this {@link PacketBuffer}
	 *
	 * @param keySupplier the supplier to read each key
	 * @param valueSupplier the supplier to read each value fitting its key
	 * @param <K> the generic key-type of the map
	 * @param <V> the generic value-type of the map
	 * @return the created map
	 */
	@Nonnull
	@UniqueReturnValue
	<K, V> Map<K,V> readMap(BiSupplier<PacketBuffer, K> keySupplier, BiSupplier<PacketBuffer, V> valueSupplier);


	/**
	 * Writes a {@link Map} into this {@link PacketBuffer}
	 * It uses {@link BiSupplier} to write the keys and values of this map using this {@link PacketBuffer}
	 *
	 * @param <K> the generic key-type of the map
	 * @param <V> the generic value-type of the map
	 * @return the current buffer instance
	 */
	<K, V> PacketBuffer writeMap(Map<K,V> map, BiConsumer<PacketBuffer, K> keySupplier, BiConsumer<PacketBuffer, V> valueSupplier);


	/**
	 * Default method to read a {@link ModifiableLocation}
	 *
	 * @return the read location
	 * @see ModifiableLocation
	 */
	@NotNull
	default ModifiableLocation<Integer> readLocation() {
		return new DefaultLocation<>(readInt(), readInt(), readInt(), readString());
	}

	/**
	 * Default method to write a {@link ModifiableLocation}
	 *
	 * @return the current buffer instance
	 * @see ModifiableLocation
	 */
	default PacketBuffer writeLocation(@NotNull ModifiableLocation<Integer> location) {
		writeInt(location.getX());
		writeInt(location.getY());
		writeInt(location.getZ());
		writeString(location.getWorld());
		return this;
	}


	/**
	 * Writes the provided {@link String} into this {@link PacketBuffer}
	 *
	 * @param string the value to write
	 * @return this buffer instance
	 */
	@Nonnull
	PacketBuffer writeString(@Nonnull String string);

	/**
	 * Reads an optional {@link String} that might be null.
	 * This method only works when using {@link #writeOptionalString(String)} on the other side
	 * It checks if the written optional-String is null. If its provided as null the buffer will not try
	 * and read a value that isn't there.
	 * Then the method returns null
	 *
	 * @return the read value or null
	 */
	@Nullable
	String readOptionalString();

	/**
	 * Writes an optional {@link String} that might be null into this {@link PacketBuffer}
	 * This method only works when using {@link #readOptionalString()} on the other side
	 * It checks if the provided optional-String is null. If it's provided as null the buffer will not try
	 * and write a value that isn't there.
	 *
	 * @return the current instance
	 */
	@Nonnull
	PacketBuffer writeOptionalString(@Nullable String string);

	/**
	 * Method to retrieve a {@link Collection} that was written to this {@link PacketBuffer}
	 *
	 * @return the found collection or create empty one
	 */
	@Nonnull
	Collection<String> readStringCollection();

	/**
	 * Writes the provided {@link Collection} to this {@link PacketBuffer}
	 *
	 * @param strings the collection to write
	 * @return the current buffer instance
	 */
	@Nonnull
	PacketBuffer writeStringCollection(@Nonnull Collection<? extends String> strings);

	/**
	 * Method to retrieve a {@link String[]} that was written to this {@link PacketBuffer}
	 *
	 * @return the found array
	 */
	@Nonnull
	String[] readStringArray();

	/**
	 * Writes the provided {@link String}[] to this {@link PacketBuffer}
	 *
	 * @param strings the array to write
	 * @return the current buffer instance
	 */
	@Nonnull
	PacketBuffer writeStringArray(@Nonnull String[] strings);

	/**
	 * Reads an optional {@link String}[] that might be null.
	 * This method only works when using {@link #writeOptionalStringArray(String[])} on the other side
	 * It checks if the written optional-array is null. If its provided as null the buffer will not try
	 * and read a value that isn't there.
	 * Then the method returns null
	 *
	 * @return the read value or null
	 */
	@Nullable
	String[] readOptionalStringArray();

	/**
	 * Writes an optional {@link String}[] that might be null into this {@link PacketBuffer}
	 * This method only works when using {@link #readOptionalStringArray()} on the other side
	 * It checks if the provided optional-array is null. If it's provided as null the buffer will not try
	 * and write a value that isn't there.
	 *
	 * @return the current instance
	 */
	@Nonnull
	PacketBuffer writeOptionalStringArray(@Nullable String[] strings);

	/**
	 * Method to retrieve a {@link UUID} that was written to this {@link PacketBuffer}
	 *
	 * @return the found id
	 */
	@Nonnull
	UUID readUniqueId();

	/**
	 * Method to write a {@link UUID} to this {@link PacketBuffer}
	 *
	 * @return the id to write
	 */
	@Nonnull
	PacketBuffer writeUniqueId(@Nonnull UUID uniqueId);

	/**
	 * Reads an optional {@link UUID} that might be null.
	 * This method only works when using {@link #writeOptionalUniqueId(UUID)}  on the other side
	 * It checks if the written optional-uuid is null. If its provided as null the buffer will not try
	 * and read a value that isn't there.
	 * Then the method returns null
	 *
	 * @return the read value or null
	 */
	@Nullable
	UUID readOptionalUniqueId();

	/**
	 * Writes an optional {@link UUID} that might be null into this {@link PacketBuffer}
	 * This method only works when using {@link #readOptionalUniqueId()}  on the other side
	 * It checks if the provided optional-uuid is null. If it's provided as null the buffer will not try
	 * and write a value that isn't there.
	 *
	 * @param uniqueId the id to write
	 * @return the current instance
	 */
	@Nonnull
	PacketBuffer writeOptionalUniqueId(@Nullable UUID uniqueId);

	/**
	 * Method to retrieve a {@link Document} that was written to this {@link PacketBuffer}
	 *
	 * @return the found value
	 */
	@Nonnull
	Document readDocument();

	/**
	 * Method to write a {@link Document} to this {@link PacketBuffer}
	 *
	 * @param document the document to write
	 * @return the id to write
	 */
	@Nonnull
	PacketBuffer writeDocument(@Nonnull Document document);

	/**
	 * Reads an optional {@link Document} that might be null.
	 * This method only works when using {@link #writeOptionalDocument(Document)} on the other side
	 * It checks if the written optional-document is null. If its provided as null the buffer will not try
	 * and read a value that isn't there.
	 * Then the method returns null
	 *
	 * @return the read value or null
	 */
	@Nullable
	Document readOptionalDocument();

	/**
	 * Writes an optional {@link Document} that might be null into this {@link PacketBuffer}
	 * This method only works when using {@link #readOptionalDocument()}   on the other side
	 * It checks if the provided optional-document is null. If it's provided as null the buffer will not try
	 * and write a value that isn't there.
	 *
	 * @param document the document to write
	 * @return the current instance
	 */
	@Nonnull
	PacketBuffer writeOptionalDocument(@Nullable Document document);

	/**
	 * This method is important to read {@link IBufferObject} from this {@link PacketBuffer}
	 * without this method we wouldn't be able to de-buffer cloud-entities like {@link CloudPlayer} or {@link CloudService}
	 * Reminder: the provided @objectClass must not be an interface. It needs to be a class with Constructor.
	 *
	 * @param objectClass the object class that extends {@link IBufferObject}
	 * @param <T> the generic type
	 * @return the read object
	 */
	@Nonnull
	<T extends IBufferObject> T readObject(@Nonnull Class<T> objectClass);

	/**
	 * This method is important to write {@link IBufferObject} from this {@link PacketBuffer}
	 * without this method we wouldn't be able to buffer cloud-entities like {@link CloudPlayer} or {@link CloudService}
	 * Reminder: the provided @objectClass must not be an interface. It needs to be a class with Constructor.
	 *
	 * @param object the object to write to this buffer
	 * @return current buffer object
	 */
	@Nonnull
	PacketBuffer writeObject(@Nonnull IBufferObject object);

	/**
	 * Reads an optional {@link IBufferObject} that might be null.
	 * This method only works when using {@link #writeOptionalObject(IBufferObject)}  on the other side
	 * It checks if the written optional-object is null. If its provided as null the buffer will not try
	 * and read a value that isn't there.
	 * Then the method returns null
	 *
	 * @return the read value or null
	 * @see #readObject(Class)
	 */
	@Nullable
	<T extends IBufferObject> T readOptionalObject(@Nonnull Class<T> objectClass);

	/**
	 * Writes an optional {@link IBufferObject} that might be null into this {@link PacketBuffer}
	 * This method only works when using {@link #readOptionalObject(Class)}   on the other side
	 * It checks if the provided optional-object is null. If it's provided as null the buffer will not try
	 * and write a value that isn't there.
	 *
	 * @param object the object to write
	 * @return the current instance
	 * @see #readObject(Class)
	 */
	@Nonnull
	PacketBuffer writeOptionalObject(@Nullable IBufferObject object);

	/**
	 * Method to retrieve a {@link Collection} that contains {@link IBufferObject}s from this {@link PacketBuffer}
	 *
	 * @param objectClass the entity object class
	 * @return the read collection
	 * @see #readObject(Class)
	 */
	@Nonnull
	<T extends IBufferObject> Collection<T> readObjectCollection(@Nonnull Class<T> objectClass);

	/**
	 * Method to retrieve a {@link Collection} that contains {@link IBufferObject}s from this {@link PacketBuffer}
	 *
	 * @param objectClass the entity object class
	 * @return the read collection
	 * @see #readObject(Class)
	 */
	@Nonnull
	<T extends IBufferObject, W extends T> Collection<T> readWrapperObjectCollection(Class<W> objectClass);

	/**
	 * Method to write a {@link Collection} that contains {@link IBufferObject}s to this {@link PacketBuffer}
	 *
	 * @param objects the objects to write
	 * @return the current buffer instance
	 */
	@Nonnull
	PacketBuffer writeObjectCollection(@Nonnull Collection<? extends IBufferObject> objects);

	/**
	 * @see #readObjectCollection(Class) Basically the same
	 *
	 * @param objectClass the object type class
	 * @param <T> the generic
	 * @return the read aray
	 */
	@Nonnull
	<T extends IBufferObject> T[] readObjectArray(@Nonnull Class<T> objectClass);

	/**
	 * @see #writeObjectCollection(Collection) Basically the same
	 *
	 * @param objects the objects to write
	 * @param <T> the generic
	 * @return the current buffer instance
	 */
	@Nonnull
	<T extends IBufferObject> PacketBuffer writeObjectArray(@Nonnull T[] objects);

	/**
	 * Method to retrieve a {@link Enum} from this {@link PacketBuffer}
	 *
	 * @param enumClass the enum class
	 * @return the read enum
	 */
	@Nonnull
	<E extends Enum<?>> E readEnum(@Nonnull Class<E> enumClass);

	/**
	 * Method to write a {@link Enum} to this {@link PacketBuffer}
	 *
	 * @param value the enum to write
	 * @return the current buffer instance
	 */
	@Nonnull
	PacketBuffer writeEnum(@Nonnull Enum<?> value);

	/**
	 * Reads an optional {@link Enum} that might be null.
	 * This method only works when using {@link #writeOptionalEnum(Enum)}   on the other side
	 * It checks if the written optional-enum is null. If its provided as null the buffer will not try
	 * and read a value that isn't there.
	 * Then the method returns null
	 *
	 * @return the read enum or null
	 */
	@Nullable
	<E extends Enum<?>> E readOptionalEnum(@Nonnull Class<E> enumClass);

	/**
	 * Writes an optional {@link Enum} that might be null into this {@link PacketBuffer}
	 * This method only works when using {@link #writeOptionalEnum(Enum)} on the other side
	 * It checks if the provided optional-enum is null. If it's provided as null the buffer will not try
	 * and write a value that isn't there.
	 *
	 * @param value the enum to write
	 * @return the current instance
	 */
	@Nonnull
	PacketBuffer writeOptionalEnum(@Nonnull Enum<?> value);

	@Nonnull
	Throwable readThrowable();

	@Nonnull
	PacketBuffer writeThrowable(@Nonnull Throwable value);

	/**
	 * Writes an optional {@link Throwable} that might be null into this {@link PacketBuffer}
	 * This method only works when using {@link #readOptionalThrowable()}  on the other side
	 * It checks if the provided optional-throwable is null. If it's provided as null the buffer will not try
	 * and write a value that isn't there.
	 *
	 * @param value the throwable to write
	 * @return the current instance
	 */
	PacketBuffer writeOptionalThrowable(Throwable value);

	/**
	 * Reads an optional {@link Throwable} that might be null.
	 * This method only works when using {@link #writeOptionalThrowable(Throwable)} on the other side
	 * It checks if the written optional-throwable is null. If its provided as null the buffer will not try
	 * and read a value that isn't there.
	 * Then the method returns null
	 *
	 * @return the read throwable or null
	 */
	Throwable readOptionalThrowable();

	/**
	 * Method to read a {@link Collection} using the given {@link Supplier} that reads the given entries one by one
	 *
	 * @param reader the reader function
	 * @param <T> the generic of the collection
	 * @return the read collection
	 */
	@Nonnull
	<T> Collection<T> readCollection(@Nonnull Supplier<T> reader);

	/**
	 * Method to write a {@link Collection} using the given {@link Consumer} that writes the given entries one by one
	 *
	 * @param writer the writer function
	 * @param <T> the generic of the collection
	 * @return the current buffer instance
	 */
	@Nonnull
	<T> PacketBuffer writeCollection(@Nonnull Collection<? extends T> collection, @Nonnull Consumer<T> writer);

	/**
	 * Method to write any object using the given {@link Supplier} that reads the given object one by one
	 *
	 * @param reader the reader function
	 * @param <T> the generic of the collection
	 * @return the read instance
	 */
	@Nullable
	<T> T readOptional(@Nonnull Supplier<T> reader);

	/**
	 * Method to write any object using the given {@link Consumer} that writes the given object one by one
	 *
	 * @param object the object to write
	 * @param writer the writer function
	 * @param <T> the generic of the collection
	 * @return the current buffer instance
	 */
	@Nonnull
	<T> PacketBuffer writeOptional(@Nullable T object, @Nonnull Consumer<T> writer);

	/**
	 * Reads an appended {@link PacketBuffer} from this {@link PacketBuffer}
	 *
	 * @return the new and read buffer instance
	 */
	@NotNull
	@UniqueReturnValue
	PacketBuffer readBuffer();

	/**
	 * Writes a {@link PacketBuffer} to this {@link PacketBuffer}
	 *
	 * @return this buffer instance
	 */
	PacketBuffer writeBuffer(@Nonnull PacketBuffer buffer);

	/**
	 * Handles the {@link Consumer} and calls {@link #writeBuffer(PacketBuffer)}
	 *
	 * @param handler the handler to handle the buffer
	 * @return the current buffer instance
	 */
	PacketBuffer append(@Nullable Consumer<? super PacketBuffer> handler);

	/**
	 * Method to write a {@link CloudPlayer} to this {@link PacketBuffer}
	 *
	 * @param cloudPlayer the player instance
	 * @return the current buffer instance
	 */
	PacketBuffer writePlayer(CloudPlayer cloudPlayer);

	/**
	 * Method to read a {@link CloudPlayer} from this {@link PacketBuffer}
	 *
	 * @return the read player
	 */
	CloudPlayer readPlayer();

	/**
	 * Method to write a {@link CloudService} to this {@link PacketBuffer}
	 *
	 * @param service the service instance
	 * @return the current buffer instance
	 */
	PacketBuffer writeService(CloudService service);

	/**
	 * Method to read a {@link CloudService} from this {@link PacketBuffer}
	 *
	 * @return the read service
	 */
	CloudService readService();

	PacketBuffer release();
}
