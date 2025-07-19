package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.function.ExceptionallySupplier;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.common.misc.Util;
import cloud.hytora.common.misc.ZipUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.exception.HytoraCloudException;
import cloud.hytora.driver.networking.protocol.PacketTypeProcessor;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.impl.UniversalCloudServer;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;


@AllArgsConstructor
@Getter
public class DefaultPacketBuffer implements PacketBuffer {

    private final ByteBuf buffer;
    private final NetworkExecutor participant;

    @Override
    public ByteBuf nettyBuffer() {
        return buffer;
    }

    @Override
    public NetworkExecutor executor() {
        return participant;
    }

    @Override
    public int length() {
        return buffer.readableBytes() + buffer.readerIndex();
    }

    @Override
    public int remaining() {
        return buffer.readableBytes();
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
    public PacketBuffer write(@Nonnull byte[] bytes, int index, int length) {
        buffer.writeBytes(bytes, index, length);
        return this;
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

    @Nonnull
    @Override
    public String readString() {
        return new String(readArray(), StandardCharsets.UTF_8);
    }

    @Override
    public <K, V> @NotNull Map<K, V> readMap(BiSupplier<PacketBuffer, K> keySupplier, BiSupplier<PacketBuffer, V> valueSupplier) {
        int size = this.readInt();
        Map<K, V> map = new ConcurrentHashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(keySupplier.supply(this), valueSupplier.supply(this));
        }
        return map;
    }

    @Override
    public <K, V> PacketBuffer writeMap(Map<K,V> map, BiConsumer<PacketBuffer, K> keySupplier, BiConsumer<PacketBuffer, V> valueSupplier) {
        this.writeInt(map.size());
        for (Map.Entry<K, V> e : map.entrySet()) {
            keySupplier.accept(this, e.getKey());
            valueSupplier.accept(this, e.getValue());
        }
        return this;
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
    public Collection<String> readStringCollection() {
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

    @Override
    public PacketBuffer writeBuffer(@NotNull PacketBuffer buffer) {

        int amount = buffer.nettyBuffer().readableBytes();
        writeVarInt(this.buffer, amount);
        this.buffer.writeBytes(buffer.asArray(), 0, amount);

        return this;
    }

    @Override
    public @NotNull PacketBuffer readBuffer() {
        return new DefaultPacketBuffer(Unpooled.wrappedBuffer(readByteArray(this.buffer, readVarInt(this.buffer))), getParticipant());
    }

    @Override
    public PacketBuffer writePacket(@NotNull IPacket packet) throws IOException {
        Task<PacketTypeProcessor> task = CloudDriver.getInstance().get(PacketTypeProcessor.class);
        task.ifPresent(packetTypeProcessor -> {
            packetTypeProcessor.writePacket(packet, this);
        });
        return this;
    }


    @Override
    public <T extends IPacket> T readPacket(Class<T> packetClass) throws IOException {
        return readPacket();
    }

    @SuppressWarnings("unchecked")
    public <T extends IPacket> T readPacket() throws IOException {
        Task<PacketTypeProcessor> task = CloudDriver.getInstance().get(PacketTypeProcessor.class);
        return task.isPresent() ? (T) task.get().readPacket(this) : null;
    }


    @Override
    public PacketBuffer append(@Nullable Consumer<? super PacketBuffer> handler) {
        if (handler == null) {
            return this;
        }
        handler.accept(this);
        return this;
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
    public PacketBuffer writeFile(File file) {

        Path path = file.toPath();
        byte[] fileBytes = file.exists() ?
                DriverUtility.get(Files.isDirectory(path), () -> ZipUtils.zipDirectory(path), (ExceptionallySupplier<byte[]>) () -> Files.readAllBytes(path))
                : new byte[0];

        this.writeBoolean(Files.isDirectory(path));
        this.writeString(path.toString());
        this.writeArray(fileBytes);

        return this;
    }


    @SneakyThrows
    void checkParent(Path current) {
        if (Files.exists(current)) {
            return;
        }
        Files.createDirectory(current);
        checkParent(current.getParent());
    }

    @Override
    public File readFile() throws IOException {

        boolean directory = this.readBoolean();
        Path path = Paths.get(this.readString());
        checkParent(path.getParent());
        byte[] bytes = this.readArray();

        if (directory) {
            if (Files.exists(path) && Files.isDirectory(path)) {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ZipUtils.unzipDirectory(bytes, path.toString());
        } else {

            if (Files.exists(path)) {
                Files.delete(path); //delete if already exists and now overriding
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
    public File readFileAndOverride(File destinationFile) throws IOException {
        if (destinationFile.exists()) {
            FileUtils.delete(destinationFile.toPath());
            Logger.constantInstance().debug("PacketBuffer#readFileAndOverride(File) hat do delete destinationFile because it existed before reading");
        }
        return readFile(destinationFile);
    }

    @Override
    public Collection<File> readFileCollection() throws IOException {
        return readCollection((ExceptionallySupplier<File>) this::readFile);
    }

    @Override
    public Collection<File> readFileCollection(File directory) throws IOException {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        List<File> files = readCollection(() -> readFileToParent(directory));
        return files;
    }

    @SneakyThrows
    @Override
    public File readFileToParent(File parent) {
        boolean directory = this.readBoolean();
        Path path = Paths.get(this.readString());
        File newFile = new File(parent, path.toFile().getName());
        byte[] bytes = this.readArray();

        if (directory) {
            if (Files.exists(path) && Files.isDirectory(path)) {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ZipUtils.unzipDirectory(bytes, path.toString());
        } else {

            if (newFile.exists()) {
                newFile.delete(); //delete if already exists and now overriding
            }



            try {
                Files.write(newFile.toPath(), bytes, StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newFile;
    }

    @Override
    public PacketBuffer writeFileCollection(Collection<File> files) throws IOException {
        return writeCollection(files, (ExceptionallyConsumer<File>) this::writeFile);
    }

    @Override
    public File readFile(File destinationFile) {


        boolean directory = this.readBoolean();
        String ignoredPath = this.readString(); //just to read it from the buffer
        Path path = destinationFile.toPath();
        byte[] bytes = this.readArray();

        if (directory) {
            if (Files.exists(path) && Files.isDirectory(path)) {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ZipUtils.unzipDirectory(bytes, path.toString());
        } else {

            try {
                if (Files.exists(path)) {
                    Files.delete(path); //delete if already exists and now overriding
                    Files.createFile(path);
                }

                Files.write(path, bytes, StandardOpenOption.CREATE_NEW);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return destinationFile;
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

    public PacketBuffer writeOptionalThrowable(Throwable value) {
        return this.writeOptional(value, this::writeThrowable);
    }

    public Throwable readOptionalThrowable() {
        return this.readOptional(this::readThrowable);
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

    @Override
    public PacketBuffer writePlayer(CloudPlayer cloudPlayer) {
        return this.writeObject(cloudPlayer);
    }

    @Override
    public CloudPlayer readPlayer() {
        return readObject(UniversalCloudPlayer.class);
    }

    @Override
    public PacketBuffer writeService(CloudService service) {
        return writeObject(service);
    }

    @Nonnull
    @Override
    public PacketBuffer release() {
        buffer.release(buffer.refCnt());
        return this;
    }

    @Override
    public CloudService readService() {
        return readObject(UniversalCloudServer.class);
    }


    @SneakyThrows
    @Override
    public void readThisObject(PacketBuffer buf, Object val) {

        if (val.getClass().getAnnotation(ProtocolBuffered.class) == null) {
            return;
        }
        for (Field declaredField : val.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            if (declaredField.isAnnotationPresent(IgnoreProtocol.class)) {
                continue;
            }
            
            if (declaredField.getType().isAnnotationPresent(ProtocolBuffered.class)) {
                Object emptyInstance = ReflectionUtils.createEmpty(declaredField.getType());
                readThisObject(buf, emptyInstance);
                declaredField.set(val, emptyInstance);
                continue;
            }
            if (Document.class.isAssignableFrom(declaredField.getType()) || declaredField.getType().getSuperclass().equals(Document.class)) {
                Document document = buf.readDocument();
                declaredField.set(val, document);
                continue;
            }
            if (Map.class.isAssignableFrom(declaredField.getType()) || declaredField.getType().getSuperclass().equals(Map.class)) {
                Class<?> keyType = (Class<?>) ((ParameterizedType) declaredField.getType().getGenericSuperclass())
                        .getActualTypeArguments()[0];
                Class<?> valueType = (Class<?>) ((ParameterizedType) declaredField.getType().getGenericSuperclass())
                        .getActualTypeArguments()[1];

                BufferHandler keyHandler = BufferType.getHandler(keyType);
                BufferHandler valueHandler = BufferType.getHandler(valueType);
                if (Util.isAnyNull(keyHandler, valueHandler)) {
                    throw new HytoraCloudException("No BufferHandler for class '" + keyType + "' or '" + valueType + "' has been registered!");
                }
                declaredField.set(val, buf.readMap(keyHandler, valueHandler));
                continue;
            }
            if (Collection.class.isAssignableFrom(declaredField.getType()) || declaredField.getType().getSuperclass().equals(Collection.class)) {
                Class<?> collectionType = (Class<?>) ((ParameterizedType) declaredField.getType().getGenericSuperclass())
                        .getActualTypeArguments()[0];

                BufferHandler handler = BufferType.getHandler(collectionType);
                if (handler == null) {
                    throw new HytoraCloudException("No BufferHandler for class '" + collectionType + "' has been registered!");
                }
                Collection<Object> objects = buf.readCollection(() -> handler.supply(buf));
                declaredField.set(val, objects);
                continue;
            }
            Class<?> fieldClass = declaredField.getType();
            BufferHandler handler = BufferType.getHandler(fieldClass);
            if (handler == null) {
                throw new HytoraCloudException("No BufferHandler for class '" + fieldClass + "' has been registered!");
            }
            declaredField.set(val, handler.supply(buf));
        }
    }
    
    @SneakyThrows
    @Override
    public void writeThisObject(PacketBuffer buf, Object val) {
        if (val.getClass().getAnnotation(ProtocolBuffered.class) == null) {
            return;
        }
        for (Field declaredField : val.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            if (declaredField.isAnnotationPresent(IgnoreProtocol.class)) {
                continue;
            }
            Object field = declaredField.get(val);
            if (field == null) {
                continue;
            }
            if (field.getClass().isAnnotationPresent(ProtocolBuffered.class)) {
                writeThisObject(buf, field);
                continue;
            }
            if (field instanceof Document) {
                Document document = (Document) field;
                buf.writeDocument(document);
                continue;
            }
            if (field instanceof Collection) {
                Collection collection = (Collection)field;
                Class<?> collectionType = (Class<?>) ((ParameterizedType) field.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];

                BufferHandler handler = BufferType.getHandler(collectionType);
                if (handler == null) {
                    throw new HytoraCloudException("No BufferHandler for class '" + collectionType + "' has been registered!");
                }
                buf.writeCollection(collection, o -> handler.accept(o, buf));
                continue;
            }
            if (field instanceof Map) {
                Map map = (Map)field;
                Class<?> keyType = (Class<?>) ((ParameterizedType) field.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
                Class<?> valueType = (Class<?>) ((ParameterizedType) field.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[1];

                BufferHandler keyHandler = BufferType.getHandler(keyType);
                BufferHandler valueHandler = BufferType.getHandler(valueType);
                if (Util.isAnyNull(keyHandler, valueHandler)) {
                    throw new HytoraCloudException("No BufferHandler for class '" + keyType + "' or '" + valueType + "' has been registered!");
                }
                buf.writeMap(map, (buffer, o) -> keyHandler.accept(o, buffer), (buffer, o) -> valueHandler.accept(o, buffer));
                continue;
            }
            Class<?> fieldClass = field.getClass();
            BufferHandler handler = BufferType.getHandler(fieldClass);
            if (handler == null) {
                throw new HytoraCloudException("No BufferHandler for class '" + fieldClass + "' has been registered!");
            }
            handler.accept(field, buf);
        }
    }

    @Override
    public void writeThisObject(Object val) {
        this.writeThisObject(this, val);
    }

    @Override
    public void readThisObject(Object val) {
        this.readThisObject(this, val);
    }

}
