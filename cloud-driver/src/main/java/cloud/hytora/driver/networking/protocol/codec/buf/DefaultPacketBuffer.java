package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.common.misc.ZipUtils;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.PacketProvider;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;


@AllArgsConstructor @Getter
public class DefaultPacketBuffer implements PacketBuffer {

	private final ByteBuf buffer;
	private final NetworkExecutor participant;

	@Override
	public ByteBuf nettyBuffer() {
		return buffer;
	}

	@Override
	public int length() {
		return buffer.readableBytes() + buffer.readerIndex();
	}

	@Override
	public int remaining() {
		return buffer.readableBytes();
	}

	@Override
	public boolean remain(int amount) {
		return remaining() >= amount;
	}

	@Nonnull
	@Override
	public byte[] asArray() {
		try {
			return buffer.array();
		} catch (Exception ex) {
		}
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.getBytes(buffer.readerIndex(), bytes);
		return bytes;
	}

	@Override
	public void read(@Nonnull byte[] bytes) {
		buffer.readBytes(bytes);
	}

	@Override
	public void read(@Nonnull OutputStream out, int length) throws IOException {
		buffer.readBytes(out, length);
	}

	@Nonnull
	@Override
	public PacketBuffer write(@Nonnull byte[] bytes) {
		buffer.writeBytes(bytes);
		return this;
	}

	@Override
	public ProtocolAddress readAddress() {
		return this.readObject(ProtocolAddress.class);
	}

	@Override
	public PacketBuffer writeAddress(@NotNull ProtocolAddress address) {
		return this.writeObject(address);
	}

	@Nonnull
	@Override
	public PacketBuffer write(@Nonnull byte[] bytes, int index, int length) {
		buffer.writeBytes(bytes, index, length);
		return this;
	}

	@Override
	public boolean readBoolean() {
		return buffer.readBoolean();
	}

	@Nonnull
	@Override
	public PacketBuffer writeBoolean(boolean value) {
		buffer.writeBoolean(value);
		return this;
	}

	@Nonnull
	@Override
	public PacketBuffer writeByte(byte b) {
		buffer.writeByte(b);
		return this;
	}

	@Override
	public byte readByte() {
		return buffer.readByte();
	}

	@Override
	public int readInt() {
		return buffer.readInt();
	}

	@Nonnull
	@Override
	public PacketBuffer writeInt(int value) {
		buffer.writeInt(value);
		return this;
	}

	@Override
	public int readVarInt() {
		return readVarInt(buffer);
	}

	@Nonnull
	@Override
	public PacketBuffer writeVarInt(int value) {
		writeVarInt(buffer, value);
		return this;
	}

	@Override
	public long readLong() {
		return buffer.readLong();
	}

	@Nonnull
	@Override
	public PacketBuffer writeLong(long value) {
		buffer.writeLong(value);
		return this;
	}

	@Override
	public long readVarLong() {
		return readVarLong(buffer);
	}

	@Nonnull
	@Override
	public PacketBuffer writeVarLong(long value) {
		writeVarLong(buffer, value);
		return this;
	}

	@Override
	public float readFloat() {
		return buffer.readFloat();
	}

	@Nonnull
	@Override
	public PacketBuffer writeFloat(float value) {
		buffer.writeFloat(value);
		return this;
	}

	@Override
	public double readDouble() {
		return buffer.readDouble();
	}

	@Nonnull
	@Override
	public PacketBuffer writeDouble(double value) {
		buffer.writeDouble(value);
		return this;
	}

	@Override
	public char readChar() {
		return buffer.readChar();
	}

	@Nonnull
	@Override
	public PacketBuffer writeChar(char value) {
		buffer.writeChar(value);
		return this;
	}

	@Nonnull
	@Override
	public PacketBuffer writeThrowable(@Nonnull Throwable value) {
		try (ByteBufOutputStream outputStream = new ByteBufOutputStream(buffer);
		     ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
			objectOutputStream.writeObject(value);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return this;
	}

	@Nonnull
	@Override
	public Throwable readThrowable() {
		try (ByteBufInputStream inputStream = new ByteBufInputStream(buffer);
		     ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
			return (Throwable) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException ex) {
			throw new WrappedException(ex);
		}
	}

	@Nonnull
	@Override
	public PacketBuffer release() {
		buffer.release(buffer.refCnt());
		return this;
	}

	@Nonnull
	@Override
	public PacketBuffer copy() {
		return new DefaultPacketBuffer(Unpooled.copiedBuffer(buffer), this.participant);
	}

	@Nonnull
	@Override
	public byte[] readArray() {
		int length = readVarInt();

		byte[] array = new byte[length];
		read(array);

		return array;
	}

	@Nonnull
	@Override
	public PacketBuffer writeArray(@Nonnull byte[] array) {
		writeVarInt(array.length);
		write(array);
		return this;
	}

	@Nullable
	@Override
	public byte[] readOptionalArray() {
		return readOptional(this::readArray);
	}

	@Nonnull
	@Override
	public PacketBuffer writeOptionalArray(@Nullable byte[] array) {
		return writeOptional(array, this::writeArray);
	}

	@Nonnull
	@Override
	public String readString() {
		return new String(readArray(), StandardCharsets.UTF_8);
	}

	@Nonnull
	@Override
	public PacketBuffer writeString(@Nonnull String string) {
		writeArray(string.getBytes(StandardCharsets.UTF_8));
		return this;
	}

	@Nullable
	@Override
	public String readOptionalString() {
		return readOptional(this::readString);
	}

	@Nonnull
	@Override
	public PacketBuffer writeOptionalString(@Nullable String string) {
		return writeOptional(string, this::writeString);
	}

	@Nonnull
	@Override
	public List<String> readStringCollection() {
		return readCollection(this::readString);
	}

	@Nonnull
	@Override
	public PacketBuffer writeStringCollection(@Nonnull Collection<? extends String> strings) {
		return writeCollection(strings, this::writeString);
	}

	@Nonnull
	@Override
	public String[] readStringArray() {
		return readArray(String.class, this::readString);
	}

	@org.jetbrains.annotations.Nullable
	@Override
	public String[] readOptionalStringArray() {
		return readOptional(this::readStringArray);
	}

	@Nonnull
	@Override
	public PacketBuffer writeStringArray(@Nonnull String[] strings) {
		return writeArray(strings, this::writeString);
	}

	@NotNull
	@Override
	public PacketBuffer writeOptionalStringArray(@org.jetbrains.annotations.Nullable String[] strings) {
		return this.writeOptional(strings, this::writeStringArray);
	}

	@Nonnull
	@Override
	public UUID readUniqueId() {
		return new UUID(readLong(), readLong());
	}

	@Nonnull
	@Override
	public PacketBuffer writeUniqueId(@Nonnull UUID uniqueId) {
		writeLong(uniqueId.getMostSignificantBits());
		writeLong(uniqueId.getLeastSignificantBits());
		return this;
	}

	@Nullable
	@Override
	public UUID readOptionalUniqueId() {
		return readOptional(this::readUniqueId);
	}

	@Nonnull
	@Override
	public PacketBuffer writeOptionalUniqueId(@Nullable UUID uniqueId) {
		return writeOptional(uniqueId, this::writeUniqueId);
	}

	@Nonnull
	@Override
	public List<UUID> readUniqueIdCollection() {
		return readCollection(this::readUniqueId);
	}

	@Nonnull
	@Override
	public PacketBuffer writeUniqueIdCollection(@Nonnull Collection<? extends UUID> uniqueIds) {
		return writeCollection(uniqueIds, this::writeUniqueId);
	}

	@Nonnull
	@Override
	public PacketBuffer writeInetAddress(@Nonnull InetAddress address) {
		writeOptionalString(address.getHostName());
		writeArray(address.getAddress());
		return this;
	}

	@Nonnull
	@Override
	public InetAddress readInetAddress() {
		try {
			return InetAddress.getByAddress(readOptionalString(), readArray());
		} catch (UnknownHostException ex) {
			throw new WrappedException(ex);
		}
	}

	@Override
	public void writeBuffer(@NotNull PacketBuffer buffer) {
		int amount = buffer.nettyBuffer().readableBytes();
		writeVarInt(this.buffer, amount);
		this.buffer.writeBytes(buffer.asArray(), 0, amount);
	}

	@Override
	public PacketBuffer readBuffer() {
		return new DefaultPacketBuffer(Unpooled.wrappedBuffer(readByteArray(this.buffer, readVarInt(this.buffer))), getParticipant());
	}

	@Override
	public PacketBuffer writePacket(IPacket packet) throws IOException {
		int id = PacketProvider.getPacketId(packet.getClass());

		if (id == -1) {
			throw new NullPointerException("Couldn't find id of packet " + packet.getClass().getSimpleName());
		}

		UUID internalQueryId = packet.transferInfo().getInternalQueryId();
		String sender = participant == null ? "unknown" : participant.getName();
		ConnectionType senderType = participant == null ? ConnectionType.UNKNOWN : participant.getType();

		//writing the custom packet buffer to store data
		this.writeBuffer(packet.buffer());

		//writing packet id
		this.writeInt(id);

		//writing destination channel
		this.writeString(packet.getDestinationChannel());

		//writing header
		this.writeDocument(packet.transferInfo().getDocument());

		//writing uuid
		this.writeUniqueId(internalQueryId);

		//writing sender info
		this.writeString(sender);
		this.writeEnum(senderType);

		//writing custom packet data
		packet.applyBuffer(BufferState.WRITE, this);

		return this;
	}


	@SuppressWarnings("unchecked")
	public <T extends IPacket> T readPacket() throws IOException {

		PacketBuffer buffer = this.readBuffer();

		int index = this.readInt();
		String channel = this.readString();
		Document header = this.readDocument();
		UUID uniqueId = this.readUniqueId();
		String name = this.readString();
		ConnectionType type = this.readEnum(ConnectionType.class);

		Class<? extends IPacket> packetClass = PacketProvider.getPacketClass(index);

		if (packetClass == null) {
			return null;
		}

		T packet = (T) ReflectionUtils.createEmpty(packetClass);

		if (packet == null) {
			throw new IllegalStateException("Couldn't construct packet for class " + packetClass);
		}

		((AbstractPacket)packet).transferInfo(new SimplePacketTransferInfo(uniqueId, new SimpleNetworkComponent(name, type), header));
		((AbstractPacket)packet).buffer(buffer);

		packet.applyBuffer(BufferState.READ, this);
		return packet;
	}
	@Nonnull
	@Override
	public Document readDocument() {
		return readObject(ProtocolDocument.class);
	}

	@Nonnull
	@Override
	public PacketBuffer writeDocument(@Nonnull Document document) {
		return writeObject(new ProtocolDocument(document));
	}

	@Nullable
	@Override
	public Document readOptionalDocument() {
		return readOptionalObject(ProtocolDocument.class);
	}

	@Nonnull
	@Override
	public PacketBuffer writeOptionalDocument(@Nullable Document document) {
		return writeOptionalObject(new ProtocolDocument(document));
	}

	@Override
	public void writeFile(File file) throws IOException {

		Path path = file.toPath();
		byte[] fileBytes;
		boolean directory = false;

		if (Files.exists(path)) {
			if (Files.isDirectory(path)) {
				fileBytes = ZipUtils.zipDirectory(path);
				directory = true;
			} else {
				try {
					fileBytes = Files.readAllBytes(path);
				} catch (IOException e) {
					fileBytes = new byte[0];
				}
			}
		} else {
			fileBytes = new byte[0];
		}

		this.writeBoolean(directory);
		this.writeString(path.toString());
		this.writeArray(fileBytes);

	}

	@Override
	public File readFile() throws IOException {

		boolean directory = this.readBoolean();
		Path path = Paths.get(this.readString());
		byte[] bytes = this.readArray();

		if (directory) {
			ZipUtils.unzipDirectory(bytes, path.toString());
		} else {
			if (Files.exists(path) && Files.isDirectory(path)) {
				try {
					Files.delete(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Path parent = path.getParent();
				if (parent != null && !Files.exists(parent)) {
					try {
						Files.createDirectories(parent);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path.toFile();
	}


	@Override
	public File readFile(File destinationFile) throws IOException {
		boolean directory = this.readBoolean();
		String ignoredPath = this.readString(); //just to read it from the buffer
		Path path = destinationFile.toPath();
		byte[] bytes = this.readArray();

		if (directory) {
			ZipUtils.unzipDirectory(bytes, path.toString());
		} else {
			if (Files.exists(path) && Files.isDirectory(path)) {
				try {
					Files.delete(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				Path parent = path.getParent();
				if (parent != null && !Files.exists(parent)) {
					try {
						Files.createDirectories(parent);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return destinationFile;
	}

	@Nonnull
	@Override
	@SuppressWarnings("unchecked")
	public List<Document> readDocumentCollection() {
		return (List<Document>) (List<? extends Document>) readObjectCollection(ProtocolDocument.class);
	}

	@Nonnull
	@Override
	public PacketBuffer writeDocumentCollection(@Nonnull Collection<? extends Document> documents) {
		return writeObjectCollection(CollectionUtils.convertCollection(documents, ProtocolDocument::new));
	}

	@Nonnull
	@Override
	public Document[] readDocumentArray() {
		return readObjectArray(ProtocolDocument.class);
	}

	@Nonnull
	@Override
	public PacketBuffer writeDocumentArray(@Nonnull Document[] documents) {
		return writeObjectArray(CollectionUtils.convertCollection(Arrays.asList(documents), ProtocolDocument::new).toArray(new IBufferObject[0]));
	}

	@Nonnull
	@Override
	public <T extends IBufferObject> T readObject(@Nonnull Class<T> objectClass) {
		try {
			T empty = ReflectionUtils.createEmpty(objectClass);
			empty.applyBuffer(BufferState.READ, this);
			return empty;
		} catch (IOException ex) {
			throw new WrappedException(ex);
		}
	}

	@Nonnull
	@Override
	public PacketBuffer writeObject(@Nonnull IBufferObject object) {
		try {
			object.applyBuffer(BufferState.WRITE, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	@Nullable
	@Override
	public <T extends IBufferObject> T readOptionalObject(@Nonnull Class<T> objectClass) {
		return readOptional(() -> readObject(objectClass));
	}

	@Nonnull
	@Override
	public PacketBuffer writeOptionalObject(@Nullable IBufferObject object) {
		return writeOptional(object, this::writeObject);
	}

	@Nonnull
	@Override
	public <T extends IBufferObject> Collection<T> readObjectCollection(@Nonnull Class<T> objectClass) {
		return readCollection(() -> readObject(objectClass));
	}

	@NotNull
	public <T extends IBufferObject, W extends T> Collection<T> readWrapperObjectCollection(Class<W> objectClass) {
		return new ArrayList<>(readObjectCollection(objectClass));
	}

	@Nonnull
	@Override
	public PacketBuffer writeObjectCollection(@Nonnull Collection<? extends IBufferObject> objects) {
		return writeCollection(objects, this::writeObject);
	}

	@Nonnull
	@Override
	public <T extends IBufferObject> T[] readObjectArray(@Nonnull Class<T> objectClass) {
		return readArray(objectClass, () -> readObject(objectClass));
	}

	@Nonnull
	@Override
	public <T extends IBufferObject> PacketBuffer writeObjectArray(@Nonnull T[] objects) {
		return writeArray(objects, this::writeObject);
	}

	@Nonnull
	@Override
	public <E extends Enum<?>> E readEnum(@Nonnull Class<E> enumClass) {
		return enumClass.getEnumConstants()[readVarInt()];
	}

	@Nonnull
	@Override
	public PacketBuffer writeEnum(@Nonnull Enum<?> value) {
		writeVarInt(value.ordinal());
		return this;
	}

	@Nullable
	@Override
	public <E extends Enum<?>> E readOptionalEnum(@Nonnull Class<E> enumClass) {
		return readOptional(() -> readEnum(enumClass));
	}

	@Nonnull
	@Override
	public PacketBuffer writeOptionalEnum(@Nonnull Enum<?> value) {
		return writeOptional(value, this::writeEnum);
	}

	@Nonnull
	@Override
	public <E extends Enum<?>> List<E> readEnumCollection(@Nonnull Class<E> enumClass) {
		return readCollection(() -> readEnum(enumClass));
	}

	@Nonnull
	@Override
	public PacketBuffer writeEnumCollection(@Nonnull Collection<? extends Enum<?>> enums) {
		return writeCollection(enums, this::writeEnum);
	}

	@Nonnull
	public <T> List<T> readCollection(@Nonnull Supplier<T> reader) {
		int length = readVarInt();
		List<T> collection = new ArrayList<>(length);

		for (int i = 0; i < length; i++) {
			collection.add(reader.get());
		}

		return collection;
	}
	@Nonnull
	public <T> PacketBuffer writeCollection(@Nonnull Collection<? extends T> collection, @Nonnull Consumer<T> writer) {
		writeVarInt(collection.size());
		for (T object : new ArrayList<>(collection)) {
			writer.accept(object);
		}
		return this;
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public <T> T[] readArray(@Nonnull Class<T> theClass, @Nonnull Supplier<T> reader) {
		int length = readVarInt();
		Object array = Array.newInstance(theClass, length);

		for (int i = 0; i < length; i++) {
			Array.set(array, i, reader.get());
		}

		return (T[]) array;
	}

	@Nonnull
	public <T> PacketBuffer writeArray(@Nonnull T[] array, @Nonnull Consumer<T> writer) {
		writeVarInt(array.length);
		for (T object : array) {
			writer.accept(object);
		}
		return this;
	}

	@Nullable
	public <T> T readOptional(@Nonnull Supplier<T> reader) {
		return readBoolean() ? reader.get() : null;
	}

	@Nonnull
	public <T> PacketBuffer writeOptional(@Nullable T object, @Nonnull Consumer<T> writer) {
		writeBoolean(object != null);
		if (object != null)
			writer.accept(object);
		return this;
	}
	@Nonnull
	public static byte[] asByteArray(@Nonnull ByteBuf buffer) {
		if (buffer.hasArray()) {
			return buffer.array();
		} else {
			byte[] bytes = new byte[buffer.readableBytes()];
			buffer.getBytes(buffer.readerIndex(), bytes);
			return bytes;
		}
	}

	@Nonnull
	public static byte[] readByteArray(@Nonnull ByteBuf buffer, @Nonnegative int size) {
		byte[] data = new byte[size];
		buffer.readBytes(data);
		return data;
	}

	public static int readVarInt(@Nonnull ByteBuf buffer) {
		return (int) readVarVariant(buffer, 5);
	}

	public static void writeVarInt(@Nonnull ByteBuf buffer, int value) {
		while (true) {
			if ((value & -128) == 0) {
				buffer.writeByte(value);
				return;
			}

			buffer.writeByte(value & 0x7F | 0x80);
			value >>>= 7;
		}

	}

	public static long readVarLong(@Nonnull ByteBuf buffer) {
		return readVarVariant(buffer, 10);
	}

	public static void writeVarLong(@Nonnull ByteBuf buffer, long value) {
		while (true) {
			if ((value & -128) == 0) {
				buffer.writeByte((int) value);
				return;
			}

			buffer.writeByte((int) value & 0x7F | 0x80);
			value >>>= 7;
		}
	}

	private static long readVarVariant(@Nonnull ByteBuf buffer, int maxReadUpperBound) {
		long i = 0;
		int maxRead = Math.min(maxReadUpperBound, buffer.readableBytes());
		for (int j = 0; j < maxRead; j++) {
			int nextByte = buffer.readByte();
			i |= (long) (nextByte & 0x7F) << j * 7;
			if ((nextByte & 0x80) != 128) {
				return i;
			}
		}

		throw new DecoderException("Bad VarInt received!");
	}

	@Nonnull
	public static ByteBuf writeString(@Nonnull ByteBuf buffer, @Nonnull String string) {
		byte[] content = string.getBytes(StandardCharsets.UTF_8);
		writeVarInt(buffer, content.length);
		buffer.writeBytes(content);
		return buffer;
	}

	@Nonnull
	public static String readString(@Nonnull ByteBuf buffer) {
		int size = readVarInt(buffer);
		return new String(readByteArray(buffer, size), StandardCharsets.UTF_8);
	}
}
