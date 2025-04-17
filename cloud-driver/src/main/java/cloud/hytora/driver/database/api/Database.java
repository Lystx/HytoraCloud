package cloud.hytora.driver.database.api;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.database.api.action.SQLColumn;
import cloud.hytora.driver.database.api.action.modify.DatabaseDeletion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertionOrUpdate;
import cloud.hytora.driver.database.api.action.modify.DatabaseUpdate;
import cloud.hytora.driver.database.api.action.query.DatabaseCountEntries;
import cloud.hytora.driver.database.api.action.query.DatabaseListTables;
import cloud.hytora.driver.database.api.action.query.DatabaseQuery;
import cloud.hytora.driver.database.api.exceptions.DatabaseAlreadyConnectedException;
import cloud.hytora.driver.database.api.exceptions.DatabaseConnectionClosedException;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import cloud.hytora.driver.database.api.impl.DatabaseConfig;
import cloud.hytora.driver.database.api.impl.EmptyDatabase;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public interface Database {


	@Nonnull
	@CheckReturnValue
	static Database empty() {
		return new EmptyDatabase(true);
	}

	@Nonnull
	@CheckReturnValue
	static Database unsupported() {
		return new EmptyDatabase(false);
	}

	boolean isConnected();

	/**
	 * Creates the connection to the database synchronously.
	 *
	 * @throws DatabaseException
	 *         If the connection could not be established
	 * @throws DatabaseAlreadyConnectedException
	 *         If this database is already {@link #isConnected() connected}
	 */
	void connect() throws DatabaseException;

	/**
	 * Creates the connection to the database synchronously.
	 * No exceptions will be thrown if the process fails.
	 *
	 * @return {@code true} if the connection was established successfully
	 */
	boolean connectSafely();

	/**
	 * Closes the connection to the database synchronously.
	 *
	 * @throws DatabaseException
	 *         If something went wrong while closing the connection to the database
	 * @throws DatabaseConnectionClosedException
	 *         If this database is not {@link #isConnected() connected}
	 */
	void disconnect() throws DatabaseException;

	/**
	 * Closes the connection to the database synchronously.
	 * No exceptions will be thrown if the process fails.
	 *
	 * @return {@code true} if the connection was closed without errors
	 */
	boolean disconnectSafely();

	void createTable(@Nonnull String name, @Nonnull SQLColumn... columns) throws DatabaseException;
	void createTableSafely(@Nonnull String name, @Nonnull SQLColumn... columns);

	@Nonnull
	default Task<Void> createTableAsync(@Nonnull String name, @Nonnull SQLColumn... columns) {
		return Task.runAsyncExceptionally(() -> createTable(name, columns));
	}

	@Nonnull
	@CheckReturnValue
    DatabaseListTables listTables();

	@Nonnull
	@CheckReturnValue
    DatabaseCountEntries countEntries(@Nonnull String table);

	@Nonnull
	@CheckReturnValue
    DatabaseQuery query(@Nonnull String table);

	@Nonnull
	@CheckReturnValue
	DatabaseUpdate update(@Nonnull String table);

	@Nonnull
	@CheckReturnValue
	DatabaseInsertion insert(@Nonnull String table);

	@Nonnull
	@CheckReturnValue
	DatabaseInsertionOrUpdate insertOrUpdate(@Nonnull String table);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion delete(@Nonnull String table);

	@Nonnull
	@CheckReturnValue
	SpecificDatabase getSpecificDatabase(@Nonnull String name);

	@Nonnull
	DatabaseConfig getConfig();

}
