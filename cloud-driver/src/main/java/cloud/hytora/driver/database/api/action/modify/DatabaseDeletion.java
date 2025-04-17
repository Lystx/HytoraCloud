package cloud.hytora.driver.database.api.action.modify;

import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.SpecificDatabase;
import cloud.hytora.driver.database.api.action.DatabaseAction;
import cloud.hytora.driver.database.api.action.hierarchy.WhereAction;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @see Database#delete(String)
 * @see SpecificDatabase#delete()
 */
public interface DatabaseDeletion extends DatabaseAction<Void>, WhereAction {

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion where(@Nonnull String field, @Nullable Object value);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion where(@Nonnull String field, @Nullable Number value);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion where(@Nonnull String field, @Nullable String value, boolean ignoreCase);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion where(@Nonnull String field, @Nullable String value);

	@Nonnull
	@CheckReturnValue
	DatabaseDeletion whereNot(@Nonnull String field, @Nullable Object value);

	@Nullable
	@Override
	Void execute() throws DatabaseException;

}
