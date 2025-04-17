package cloud.hytora.driver.database.api.exceptions;

import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.driver.database.api.action.DatabaseAction;

import javax.annotation.Nonnull;

/**
 *
 * @see DatabaseException
 *
 * @see DatabaseAction#executeUnsigned()
 */
public class UnsignedDatabaseException extends WrappedException {

	public UnsignedDatabaseException(@Nonnull DatabaseException cause) {
		super(cause);
	}

	@Nonnull
	@Override
	public DatabaseException getCause() {
		return (DatabaseException) super.getCause();
	}
}
