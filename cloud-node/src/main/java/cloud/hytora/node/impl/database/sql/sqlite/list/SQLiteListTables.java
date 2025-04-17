package cloud.hytora.node.impl.database.sql.sqlite.list;

import cloud.hytora.driver.database.api.action.query.DatabaseListTables;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import cloud.hytora.node.impl.database.sql.abstraction.AbstractSQLDatabase;

import javax.annotation.Nonnull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SQLiteListTables implements DatabaseListTables {

	protected final AbstractSQLDatabase database;

	public SQLiteListTables(@Nonnull AbstractSQLDatabase database) {
		this.database = database;
	}

	@Nonnull
	@Override
	public List<String> execute() throws DatabaseException {
		try {
			PreparedStatement statement = database.prepare("SELECT name FROM sqlite_master WHERE type = 'table'");
			ResultSet result = statement.executeQuery();

			List<String> tables = new ArrayList<>();
			while (result.next()) {
				tables.add(result.getString(1));
			}
			return tables;
		} catch (Exception ex) {
			throw new DatabaseException(ex);
		}
	}

}
