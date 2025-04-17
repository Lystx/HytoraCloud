package cloud.hytora.node.impl.database.sql.abstraction;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.api.impl.DatabaseConfig;
import cloud.hytora.driver.database.api.action.SQLColumn;
import cloud.hytora.driver.database.api.action.modify.DatabaseDeletion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertionOrUpdate;
import cloud.hytora.driver.database.api.action.modify.DatabaseUpdate;
import cloud.hytora.driver.database.api.action.query.DatabaseCountEntries;
import cloud.hytora.driver.database.api.action.query.DatabaseQuery;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import cloud.hytora.driver.database.api.AbstractDatabase;
import cloud.hytora.node.impl.database.sql.abstraction.count.SQLCountEntries;
import cloud.hytora.node.impl.database.sql.abstraction.deletion.SQLDeletion;
import cloud.hytora.node.impl.database.sql.abstraction.insertion.SQLInsertion;
import cloud.hytora.node.impl.database.sql.abstraction.insertorupdate.SQLInsertionOrUpdate;
import cloud.hytora.node.impl.database.sql.abstraction.query.SQLQuery;
import cloud.hytora.node.impl.database.sql.abstraction.update.SQLUpdate;
import cloud.hytora.node.impl.database.sql.abstraction.where.SQLWhere;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public abstract class AbstractSQLDatabase extends AbstractDatabase {

	protected Connection connection;

	public AbstractSQLDatabase(@Nonnull DatabaseConfig config) {
		super(config);
	}

	@Override
	public void disconnect0() throws Exception {
		connection.close();
		connection = null;
	}

	@Override
	public void connect0() throws Exception {
		connection = DriverManager.getConnection(createUrl(), config.getUser(), config.getPassword());
	}

	protected abstract String createUrl();

	@Override
	public boolean isConnected() {
		try {
			if (connection == null) return false;
			connection.isClosed();
			return true;
		} catch (SQLException ex) {
			CloudDriver.getInstance().getLogger().error("Could not check connection state: " + ex.getMessage());
			return false;
		}
	}

	@Override
	public void createTable(@Nonnull String name, @Nonnull SQLColumn... columns) throws DatabaseException {
		try {
			StringBuilder command = new StringBuilder();
			command.append("CREATE TABLE IF NOT EXISTS `");
			command.append(name);
			command.append("` (");
			{
				int index = 0;
				for (SQLColumn column : columns) {
					if (index > 0) command.append(", ");
					command.append(column.toString());
					index++;
				}
			}
			command.append(")");

			PreparedStatement statement = prepare(command.toString());
			statement.execute();
		} catch (Exception ex) {
			throw new DatabaseException(ex);
		}
	}

	@Nonnull
	@Override
	public DatabaseCountEntries countEntries(@Nonnull String table) {
		return new SQLCountEntries(this, table);
	}

	@Nonnull
	@Override
	public DatabaseQuery query(@Nonnull String table) {
		return new SQLQuery(this, table);
	}

	@Nonnull
	public DatabaseQuery query(@Nonnull String table, @Nonnull Map<String, SQLWhere> where) {
		return new SQLQuery(this, table, where);
	}

	@Nonnull
	@Override
	public DatabaseUpdate update(@Nonnull String table) {
		return new SQLUpdate(this, table);
	}

	@Nonnull
	@Override
	public DatabaseInsertion insert(@Nonnull String table) {
		return new SQLInsertion(this, table);
	}

	@Nonnull
	public DatabaseInsertion insert(@Nonnull String table, @Nonnull Map<String, Object> values) {
		return new SQLInsertion(this, table, values);
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate insertOrUpdate(@Nonnull String table) {
		return new SQLInsertionOrUpdate(this, table);
	}

	@Nonnull
	@Override
	public DatabaseDeletion delete(@Nonnull String table) {
		return new SQLDeletion(this, table);
	}

	@Nonnull
	public PreparedStatement prepare(@Nonnull CharSequence command, @Nonnull Object... args) throws SQLException, DatabaseException {
		checkConnection();
		PreparedStatement statement = connection.prepareStatement(command.toString());
		SQLHelper.fillParams(statement, args);
		return statement;
	}

}