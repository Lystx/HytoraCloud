package cloud.hytora.node.impl.database.mongo.update;

import cloud.hytora.node.impl.database.mongo.misc.BsonUtils;
import cloud.hytora.node.impl.database.mongo.misc.MongoUtils;
import cloud.hytora.node.impl.database.mongo.where.MongoDBWhere;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import cloud.hytora.driver.database.api.action.modify.DatabaseUpdate;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import cloud.hytora.node.impl.database.mongo.MongoDBDatabase;
import cloud.hytora.node.impl.database.mongo.where.ObjectWhere;
import cloud.hytora.node.impl.database.mongo.where.StringIgnoreCaseWhere;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class MongoDBUpdate implements DatabaseUpdate {

	protected final MongoDBDatabase database;
	protected final String collection;
	protected final Map<String, MongoDBWhere> where;
	protected final Map<String, Object> values;

	public MongoDBUpdate(@Nonnull MongoDBDatabase database, @Nonnull String collection) {
		this.database = database;
		this.collection = collection;
		this.where = new HashMap<>();
		this.values = new HashMap<>();
	}

	public MongoDBUpdate(@Nonnull MongoDBDatabase database, @Nonnull String collection, @Nonnull Map<String, MongoDBWhere> where, @Nonnull Map<String, Object> values) {
		this.database = database;
		this.collection = collection;
		this.where = where;
		this.values = values;
	}

	@Nonnull
	@Override
	public DatabaseUpdate where(@Nonnull String field, @Nullable Object value) {
		where.put(field, new ObjectWhere(field, value, Filters::eq));
		return this;
	}

	@Nonnull
	@Override
	public DatabaseUpdate where(@Nonnull String field, @Nullable Number value) {
		return where(field, (Object) value);
	}

	@Nonnull
	@Override
	public DatabaseUpdate where(@Nonnull String field, @Nullable String value) {
		return where(field, (Object) value);
	}

	@Nonnull
	@Override
	public DatabaseUpdate where(@Nonnull String field, @Nullable String value, boolean ignoreCase) {
		if (!ignoreCase) return where(field, value);
		if (value == null) throw new NullPointerException("Cannot use where ignore case with null value");
		where.put(field, new StringIgnoreCaseWhere(field, value));
		return this;
	}

	@Nonnull
	@Override
	public DatabaseUpdate whereNot(@Nonnull String field, @Nullable Object value) {
		where.put(field, new ObjectWhere(field, value, Filters::ne));
		return this;
	}

	@Nonnull
	@Override
	public DatabaseUpdate set(@Nonnull String field, @Nullable Object value) {
		values.put(field, MongoUtils.packObject(value));
		return this;
	}

	@Override
	public Void execute() throws DatabaseException {
		try {
			MongoCollection<Document> collection = database.getCollection(this.collection);

			Document filter = new Document();
			UpdateOptions options = new UpdateOptions();

			for (MongoDBWhere where : where.values()) {
				Bson whereBson = where.toBson();
				BsonDocument asBsonDocument = BsonUtils.convertBsonToBsonDocument(whereBson);
				filter.putAll(asBsonDocument);

				Collation collation = where.getCollation();
				if (collation != null)
					options.collation(collation);
			}

			Document newDocument = new Document();
			for (Entry<String, Object> entry : values.entrySet()) {
				newDocument.put(entry.getKey(), MongoUtils.packObject(entry.getValue()));
			}

			BasicDBObject update = new BasicDBObject();
			update.put("$set", newDocument);

			collection.updateMany(filter, update, options);
			return null;
		} catch (Exception ex) {
			throw new DatabaseException(ex);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MongoDBUpdate that = (MongoDBUpdate) o;
		return database.equals(that.database)
				&& collection.equals(that.collection)
				&& where.equals(that.where)
				&& values.equals(that.values);
	}

	@Override
	public int hashCode() {
		return Objects.hash(database, collection, where, values);
	}

}
