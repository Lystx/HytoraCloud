package cloud.hytora.driver.database.api.action.query;

import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.SpecificDatabase;
import cloud.hytora.driver.database.api.action.DatabaseAction;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @see Database#countEntries(String)
 * @see SpecificDatabase#countEntries()
 */
public interface DatabaseCountEntries extends DatabaseAction<Long> {

	@Nonnull
	@Override
	@Nonnegative
	Long execute() throws DatabaseException;

}
