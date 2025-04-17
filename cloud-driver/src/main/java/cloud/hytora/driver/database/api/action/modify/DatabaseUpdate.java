package cloud.hytora.driver.database.api.action.modify;

import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.SpecificDatabase;
import cloud.hytora.driver.database.api.action.DatabaseAction;
import cloud.hytora.driver.database.api.action.hierarchy.SetAction;
import cloud.hytora.driver.database.api.action.hierarchy.WhereAction;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @see Database#update(String)
 * @see SpecificDatabase#update()
 */
public interface DatabaseUpdate extends DatabaseAction<Void>, WhereAction, SetAction {

	@Nonnull
	@CheckReturnValue
	DatabaseUpdate where(@Nonnull String field, @Nullable Object value);

	@Nonnull
	@CheckReturnValue
	DatabaseUpdate where(@Nonnull String field, @Nullable Number value);

	@Nonnull
	@CheckReturnValue
	DatabaseUpdate where(@Nonnull String field, @Nullable String value, boolean ignoreCase);

	@Nonnull
	@CheckReturnValue
	DatabaseUpdate where(@Nonnull String field, @Nullable String value);

	@Nonnull
	@CheckReturnValue
	DatabaseUpdate whereNot(@Nonnull String field, @Nullable Object value);

	@Nonnull
	@CheckReturnValue
	DatabaseUpdate set(@Nonnull String field, @Nullable Object value);

	@Nullable
	@Override
	Void execute() throws DatabaseException;

}
