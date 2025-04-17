package cloud.hytora.driver.database.api.action.query;

import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.action.DatabaseAction;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;

import javax.annotation.Nonnull;
import java.util.List;

/**
 *
 * @see Database#listTables()
 */
public interface DatabaseListTables extends DatabaseAction<List<String>> {

	@Nonnull
	@Override
	List<String> execute() throws DatabaseException;

}
