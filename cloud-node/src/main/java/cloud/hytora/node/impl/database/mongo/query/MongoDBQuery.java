package cloud.hytora.node.impl.database.mongo.query;

import cloud.hytora.driver.database.api.impl.DefaultExecutedQuery;
import cloud.hytora.node.impl.database.mongo.misc.MongoUtils;
import cloud.hytora.node.impl.database.mongo.where.MongoDBWhere;
import cloud.hytora.node.impl.database.mongo.where.ObjectWhere;
import cloud.hytora.node.impl.database.mongo.where.StringIgnoreCaseWhere;
import cloud.hytora.node.impl.database.mongo.MongoDBDatabase;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import cloud.hytora.driver.database.api.action.query.DatabaseQuery;
import cloud.hytora.driver.database.api.action.query.ExecutedQuery;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MongoDBQuery implements DatabaseQuery {

	protected final MongoDBDatabase database;
	protected final String collection;
	protected final Map<String, MongoDBWhere> where;
	protected Order order;
	protected String orderBy;

	public MongoDBQuery(@Nonnull MongoDBDatabase database, @Nonnull String collection) {
		this.database = database;
		this.collection = collection;
		this.where = new HashMap<>();
	}

	public MongoDBQuery(@Nonnull MongoDBDatabase database, @Nonnull String collection, @Nonnull Map<String, MongoDBWhere> where) {
		this.database = database;
		this.collection = collection;
		this.where = where;
	}

	@Nonnull
	@Override
	public DatabaseQuery where(@Nonnull String field, @Nullable Object value) {
		where.put(field, new ObjectWhere(field, value, Filters::eq));
		return this;
	}

	@Nonnull
	@Override
	public DatabaseQuery where(@Nonnull String field, @Nullable Number value) {
		return where(field, (Object) value);
	}

	@Nonnull
	@Override
	public DatabaseQuery where(@Nonnull String field, @Nullable String value) {
		return where(field, (Object) value);
	}

	@Nonnull
	@Override
	public DatabaseQuery where(@Nonnull String field, @Nullable String value, boolean ignoreCase) {
		if (!ignoreCase) return where(field, value);
		if (value == null) throw new NullPointerException("Cannot use where ignore case with null value");
		where.put(field, new StringIgnoreCaseWhere(field, value));
		return this;
	}

	@Nonnull
	@Override
	public DatabaseQuery whereNot(@Nonnull String field, @Nullable Object value) {
		where.put(field, new ObjectWhere(field, value, Filters::ne));
		return this;
	}

	@Nonnull
	@Override
	public DatabaseQuery orderBy(@Nonnull String column, @Nonnull Order order) {
		this.orderBy = column;
		this.order = order;
		return this;
	}

	@Nonnull
	@Override
	public DatabaseQuery select(@Nonnull String... selection) {
		return this;
	}

	@Nonnull
	@Override
	public ExecutedQuery execute() throws DatabaseException {
		try {
			FindIterable<Document> iterable = database.getCollection(collection).find();
			MongoUtils.applyWhere(iterable, where);
			MongoUtils.applyOrder(iterable, orderBy, order);

			List<Document> documents = iterable.into(new ArrayList<>());
			return createExecutedQuery(documents);
		} catch (Exception ex) {
			throw new DatabaseException(ex);
		}
	}

	@Nonnull
	private ExecutedQuery createExecutedQuery(@Nonnull List<Document> documents) {
		List<cloud.hytora.document.Document> results = new ArrayList<>(documents.size());
		for (Document document : documents) {
			results.add(new MongoDBResult(document));
		}

		return new DefaultExecutedQuery(results);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MongoDBQuery that = (MongoDBQuery) o;
		return database.equals(that.database) && collection.equals(that.collection) && where.equals(that.where) && order == that.order && Objects.equals(orderBy, that.orderBy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(database, collection, where, order, orderBy);
	}

}
