package cloud.hytora.driver.database.api.impl;

import cloud.hytora.common.function.ExceptionallySupplier;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.database.api.*;
import cloud.hytora.driver.database.api.action.SQLColumn;
import cloud.hytora.driver.database.api.action.modify.DatabaseDeletion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertionOrUpdate;
import cloud.hytora.driver.database.api.action.modify.DatabaseUpdate;
import cloud.hytora.driver.database.api.action.query.DatabaseCountEntries;
import cloud.hytora.driver.database.api.action.query.DatabaseListTables;
import cloud.hytora.driver.database.api.action.query.DatabaseQuery;
import cloud.hytora.driver.database.api.action.query.ExecutedQuery;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class EmptyDatabase implements Database {

	private final boolean silent;

	public EmptyDatabase(boolean silent) {
		this.silent = silent;
	}

	public boolean isSilent() {
		return silent;
	}

	protected void exception(@Nonnull String message) {
		throw new UnsupportedOperationException(message);
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public void connect() throws DatabaseException {
		if (!silent)
			exception("Cannot connect with a NOP Database");
	}

	@Override
	public boolean connectSafely() {
		return false;
	}

	@Override
	public void disconnect() throws DatabaseException {
		if (!silent)
			exception("Cannot disconnect from a NOP Database");
	}

	@Override
	public boolean disconnectSafely() {
		return false;
	}

	@Override
	public void createTable(@Nonnull String name, @Nonnull SQLColumn... columns) throws DatabaseException {
		if (!silent)
			exception("Cannot create tables from a NOP Database");
	}

	@Override
	public void createTableSafely(@Nonnull String name, @Nonnull SQLColumn... columns) {
	}

	@Nonnull
	@Override
	public DatabaseListTables listTables() {
		if (!silent)
			exception("Cannot list tables of a NOP Database");

		return new EmptyListTables();
	}

	@Nonnull
	@Override
	public DatabaseCountEntries countEntries(@Nonnull String table) {
		if (!silent)
			exception("Cannot count entries of a NOP Database");

		return new EmptyCountEntries();
	}

	@Nonnull
	@Override
	public DatabaseQuery query(@Nonnull String table) {
		if (!silent)
			exception("Cannot query in a NOP Database");

		return new EmptyDatabaseQuery();
	}

	@Nonnull
	@Override
	public DatabaseUpdate update(@Nonnull String table) {
		if (!silent)
			exception("Cannot update in a NOP Database");

		return new EmptyVoidAction();
	}

	@Nonnull
	@Override
	public DatabaseInsertion insert(@Nonnull String table) {
		if (!silent)
			exception("Cannot insert into a NOP Database");

		return new EmptyVoidAction();
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate insertOrUpdate(@Nonnull String table) {
		if (!silent)
			exception("Cannot inset or update into a NOP Database");

		return new EmptyVoidAction();
	}

	@Nonnull
	@Override
	public DatabaseDeletion delete(@Nonnull String table) {
		if (!silent)
			exception("Cannot delete from a NOP Database");

		return new EmptyVoidAction();
	}

	@Nonnull
	@Override
	public SpecificDatabase getSpecificDatabase(@Nonnull String name) {
		return new DefaultSpecificDatabase(this, name);
	}

	@Nonnull
	@Override
	public DatabaseConfig getConfig() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "EmptyDatabase[silent=" + silent + "]";
	}

	public static class EmptyDatabaseQuery implements DatabaseQuery {

		@Nonnull
		@Override
		public DatabaseQuery where(@Nonnull String field, @Nullable Object object) {
			return this;
		}

		@Nonnull
		@Override
		public DatabaseQuery where(@Nonnull String field, @Nullable Number value) {
			return this;
		}

		@Nonnull
		@Override
		public DatabaseQuery where(@Nonnull String field, @Nullable String value, boolean ignoreCase) {
			return this;
		}

		@Nonnull
		@Override
		public DatabaseQuery where(@Nonnull String field, @Nullable String value) {
			return this;
		}

		@Nonnull
		@Override
		public DatabaseQuery whereNot(@Nonnull String field, @Nullable Object value) {
			return this;
		}

		@Nonnull
		@Override
		public DatabaseQuery select(@Nonnull String... selection) {
			return this;
		}

		@Nonnull
		@Override
		public DatabaseQuery orderBy(@Nonnull String field, @Nonnull Order order) {
			return this;
		}

		@Nonnull
		@Override
		public ExecutedQuery execute() throws DatabaseException {
			return new DefaultExecutedQuery(Collections.emptyList());
		}

		@Nonnull
		@Override
		public Task<ExecutedQuery> executeAsync() {
			return Task.callSync(this::execute);
		}

	}

	public static class EmptyVoidAction implements DatabaseDeletion, DatabaseInsertion, DatabaseUpdate, DatabaseInsertionOrUpdate {

		@Nonnull
		@Override
		public EmptyVoidAction where(@Nonnull String field, @Nullable Object value) {
			return this;
		}

		@Nonnull
		@Override
		public EmptyVoidAction where(@Nonnull String field, @Nullable Number value) {
			return this;
		}

		@Nonnull
		@Override
		public EmptyVoidAction where(@Nonnull String field, @Nullable String value, boolean ignoreCase) {
			return this;
		}

		@Nonnull
		@Override
		public EmptyVoidAction where(@Nonnull String field, @Nullable String value) {
			return this;
		}

		@Nonnull
		@Override
		public EmptyVoidAction whereNot(@Nonnull String field, @Nullable Object value) {
			return this;
		}

		@Nonnull
		@Override
		public EmptyVoidAction set(@Nonnull String field, @Nullable Object value) {
			return this;
		}

		@Override
		public Void execute() throws DatabaseException {
			return null;
		}

		@Nonnull
		@Override
		public Task<Void> executeAsync() {
			return Task.callAsync((ExceptionallySupplier<Void>) Void.TYPE::newInstance);
		}

	}

	public static class EmptyCountEntries implements DatabaseCountEntries {

		@Nonnull
		@Override
		public Long execute() throws DatabaseException {
			return 0L;
		}

		@Nonnull
		@Override
		public Task<Long> executeAsync() {
			return Task.build(0L);
		}

	}

	public static class EmptyListTables implements DatabaseListTables {

		@Nonnull
		@Override
		public List<String> execute() throws DatabaseException {
			return Collections.emptyList();
		}

		@Nonnull
		@Override
		public Task<List<String>> executeAsync() {
			return Task.build(Collections.emptyList());
		}

	}

}
