package cloud.hytora.node.impl.database.mongo.list;

import cloud.hytora.driver.database.api.action.query.DatabaseListTables;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import cloud.hytora.node.impl.database.mongo.MongoDBDatabase;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MongoDBListTables implements DatabaseListTables {

	protected final MongoDBDatabase database;

	public MongoDBListTables(@Nonnull MongoDBDatabase database) {
		this.database = database;
	}

	@Nonnull
	@Override
	public List<String> execute() throws DatabaseException {
		try {
			return database.getDatabase().listCollectionNames().into(new ArrayList<>());
		} catch (Exception ex) {
			throw new DatabaseException(ex);
		}
	}

}
