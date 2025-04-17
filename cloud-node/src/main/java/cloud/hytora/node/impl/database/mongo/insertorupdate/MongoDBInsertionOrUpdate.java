package cloud.hytora.node.impl.database.mongo.insertorupdate;

import cloud.hytora.driver.database.api.action.modify.DatabaseInsertionOrUpdate;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import cloud.hytora.node.impl.database.mongo.MongoDBDatabase;
import cloud.hytora.node.impl.database.mongo.misc.BsonUtils;
import cloud.hytora.node.impl.database.mongo.update.MongoDBUpdate;
import cloud.hytora.node.impl.database.mongo.where.MongoDBWhere;
import org.bson.BsonDocument;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map.Entry;

public class MongoDBInsertionOrUpdate extends MongoDBUpdate implements DatabaseInsertionOrUpdate {

	public MongoDBInsertionOrUpdate(@Nonnull MongoDBDatabase database, @Nonnull String collection) {
		super(database, collection);
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate where(@Nonnull String field, @Nullable String value, boolean ignoreCase) {
		super.where(field, value, ignoreCase);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate where(@Nonnull String field, @Nullable Object value) {
		super.where(field, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate where(@Nonnull String field, @Nullable String value) {
		super.where(field, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate where(@Nonnull String field, @Nullable Number value) {
		super.where(field, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate whereNot(@Nonnull String field, @Nullable Object value) {
		super.whereNot(field, value);
		return this;
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate set(@Nonnull String field, @Nullable Object value) {
		super.set(field, value);
		return this;
	}

	@Override
	public Void execute() throws DatabaseException {
		if (database.query(collection, where).execute().isSet()) {
			return super.execute();
		} else {
			Document document = new Document(values);
			for (Entry<String, MongoDBWhere> entry : where.entrySet()) {
				BsonDocument bson = BsonUtils.convertBsonToBsonDocument(entry.getValue().toBson());
				document.putAll(bson);
			}

			database.insert(collection, document).execute();
			return null;
		}
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

}
