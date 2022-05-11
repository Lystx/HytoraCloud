package cloud.hytora.document.bson;

import com.mongodb.MongoClient;
import cloud.hytora.common.misc.ReflectionUtils;
import org.bson.*;
import org.bson.BsonDocument;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;


public class BsonHelper {

	public static <T> String toJson(@Nonnull T value, boolean pretty) {
		StringWriter writer = new StringWriter();
		toJson(value, pretty, writer);
		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> void toJson(@Nonnull T value, boolean pretty, @Nonnull Writer writer) {
		Codec<T> codec = MongoClient.getDefaultCodecRegistry().get((Class<T>) value.getClass());
		codec.encode(
			new JsonWriter(writer, JsonWriterSettings.builder().indent(pretty).build()),
			value, EncoderContext.builder().build()
		);
	}


	@Nonnull
	@SuppressWarnings("unchecked")
	public static BsonValue toBsonValue(@Nullable Object value) {
		if (value == null) {
			return BsonNull.VALUE;
		} else if (value instanceof BsonValue) {
			return (BsonValue) value;
		} else if (value instanceof Bson) {
			return toBsonDocument((Bson) value);
		} else if (value instanceof String) {
			return new BsonString((String) value);
		} else if (value instanceof Boolean) {
			return new BsonBoolean((Boolean) value);
		} else if (value instanceof ObjectId) {
			return new BsonObjectId((ObjectId) value);
		} else if (value instanceof Double) {
			return new BsonDouble((Double) value);
		} else if (value instanceof Float) {
			return new BsonDouble((Float) value);
		} else if (value instanceof Long) {
			return new BsonInt64((Long) value);
		} else if (value instanceof Integer) {
			return new BsonInt32((Integer) value);
		} else if (value instanceof Number) {
			return new BsonInt32(((Number) value).intValue());
		} else if (value instanceof byte[]) {
			return new BsonBinary((byte[]) value);
		} else if (value instanceof Iterable) {
			return toBsonArray((Iterable<?>) value);
		} else if (value.getClass().isArray()) {
			return toBsonArray(value);
		} else if (value instanceof Map) {
			return toBsonDocument((Map<String, Object>) value);
		} else {
			throw new IllegalStateException("Cannot convert " + value.getClass().getName() + " to some BsonValue");
		}
	}

	@Nonnull
	public static BsonDocument toBsonDocument(@Nonnull Map<String, Object> values) {
		BsonDocument document = new BsonDocument();
		for (Entry<String, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			document.put(entry.getKey(), toBsonValue(value));
		}
		return document;
	}

	@Nonnull
	public static BsonDocument toBsonDocument(@Nonnull Bson bson) {
		return bson.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
	}

	@Nonnull
	public static BsonArray toBsonArray(@Nonnull Iterable<?> iterable) {
		BsonArray bsonArray = new BsonArray();
		iterable.forEach(object -> bsonArray.add(toBsonValue(object)));
		return bsonArray;
	}

	@Nonnull
	public static BsonArray toBsonArray(@Nonnull Object array) {
		BsonArray bsonArray = new BsonArray();
		ReflectionUtils.forEachInArray(array, object -> bsonArray.add(toBsonValue(object)));
		return bsonArray;
	}

	private BsonHelper() {}
}
