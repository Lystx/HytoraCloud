package cloud.hytora.driver.database.api.exceptions;

/**
 * This exception in thrown, when a database tries to connect but is already connected.
 *
 * 
 * @since 1.1
 */
public class DatabaseAlreadyConnectedException extends DatabaseException {

	public DatabaseAlreadyConnectedException() {
		super("Database already connected");
	}

}
