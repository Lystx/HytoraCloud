package cloud.hytora.driver.database.api.exceptions;

import cloud.hytora.driver.database.api.action.DatabaseAction;

import javax.annotation.Nonnull;

/**
 *
 * @see DatabaseAlreadyConnectedException
 * @see DatabaseConnectionClosedException
 * @see DatabaseUnsupportedFeatureException
 *
 * @see DatabaseAction#execute()
 */
public class DatabaseException extends Exception {

	protected DatabaseException() {
		super();
	}

	public DatabaseException(@Nonnull String message) {
		super(message);
	}

	public DatabaseException(@Nonnull Throwable cause) {
		super(cause);
	}

	public DatabaseException(@Nonnull String message, @Nonnull Throwable cause) {
		super(message, cause);
	}
}
