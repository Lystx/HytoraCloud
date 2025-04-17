package cloud.hytora.node.impl.database.mongo.misc;

import cloud.hytora.common.misc.BukkitReflectionSerializationUtils;
import cloud.hytora.document.JsonEntity;
import cloud.hytora.driver.database.api.action.hierarchy.OrderedAction;
import cloud.hytora.node.impl.database.mongo.where.MongoDBWhere;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.Sorts;
import org.bson.BsonDocument;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public final class MongoUtils {

	private MongoUtils() {
	}

	public static void applyWhere(@Nonnull FindIterable<Document> iterable, @Nonnull Map<String, MongoDBWhere> where) {
		for (Entry<String, MongoDBWhere> entry : where.entrySet()) {
			MongoDBWhere value = entry.getValue();
			iterable.filter(value.toBson());

			Collation collation = value.getCollation();
			if (collation != null)
				iterable.collation(collation);
		}
	}

	public static void applyOrder(@Nonnull FindIterable<Document> iterable, @Nullable String orderBy, @Nullable OrderedAction.Order order) {
		if (order == null || orderBy == null) return;
		switch (order) {
			case HIGHEST:
				iterable.sort(Sorts.descending(orderBy));
				break;
			case LOWEST:
				iterable.sort(Sorts.ascending(orderBy));
				break;
		}
	}

	@Nullable
	public static Object packObject(@Nullable Object value) {
		if (value == null) {
			return null;
		} else if (value instanceof JsonEntity) {
			String json = ((JsonEntity) value).asRawJsonString();
			return Document.parse(json);
		} else if (BukkitReflectionSerializationUtils.isSerializable(value.getClass())) {
			Map<String, Object> values = BukkitReflectionSerializationUtils.serializeObject(value);
			if (values == null) return null;
			BsonDocument bson = new BsonDocument();
			BsonUtils.setDocumentProperties(bson, values);
			return bson;
		} else if (value instanceof UUID) {
			return ((UUID) value).toString();
		} else {
			return value;
		}
	}

}
