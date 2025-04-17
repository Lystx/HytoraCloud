package cloud.hytora.driver.database.api.action.query;

import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.SpecificDatabase;
import cloud.hytora.driver.database.api.action.DatabaseAction;
import cloud.hytora.driver.database.api.action.hierarchy.OrderedAction;
import cloud.hytora.driver.database.api.action.hierarchy.WhereAction;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @see Database#query(String)
 * @see SpecificDatabase#query()
 */
public interface DatabaseQuery extends DatabaseAction<ExecutedQuery>, WhereAction, OrderedAction {

	@Nonnull
	@CheckReturnValue
	DatabaseQuery where(@Nonnull String field, @Nullable Object object);

	@Nonnull
	@CheckReturnValue
	DatabaseQuery where(@Nonnull String field, @Nullable Number value);

	@Nonnull
	@CheckReturnValue
	DatabaseQuery where(@Nonnull String field, @Nullable String value, boolean ignoreCase);

	@Nonnull
	@CheckReturnValue
	DatabaseQuery where(@Nonnull String field, @Nullable String value);

	@Nonnull
	@CheckReturnValue
	DatabaseQuery whereNot(@Nonnull String field, @Nullable Object value);

	@Nonnull
	@CheckReturnValue
	DatabaseQuery select(@Nonnull String... selection);

	@Nonnull
	@CheckReturnValue
	DatabaseQuery orderBy(@Nonnull String field, @Nonnull Order order);

	@Nonnull
	@Override
	@CheckReturnValue
	ExecutedQuery execute() throws DatabaseException;

}
