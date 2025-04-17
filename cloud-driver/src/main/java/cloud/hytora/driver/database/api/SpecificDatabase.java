package cloud.hytora.driver.database.api;

import cloud.hytora.driver.database.api.action.modify.DatabaseDeletion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertionOrUpdate;
import cloud.hytora.driver.database.api.action.modify.DatabaseUpdate;
import cloud.hytora.driver.database.api.action.query.DatabaseCountEntries;
import cloud.hytora.driver.database.api.action.query.DatabaseQuery;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Represents a table/collection of a database
 *
 *
 * @see Database
 * @see Database#getSpecificDatabase(String)
 */
public interface SpecificDatabase {

	boolean isConnected();

	@Nonnull
	String getName();

	/**
	 * @see Database#countEntries(String)
	 */
	@Nonnull
	@CheckReturnValue
    DatabaseCountEntries countEntries();

	/**
	 * @see Database#query(String)
	 */
	@Nonnull
	@CheckReturnValue
    DatabaseQuery query();

	/**
	 * @see Database#update(String)
	 */
	@Nonnull
	@CheckReturnValue
	DatabaseUpdate update();

	/**
	 * @see Database#insert(String)
	 */
	@Nonnull
	@CheckReturnValue
	DatabaseInsertion insert();

	/**
	 * @see Database#insertOrUpdate(String)
	 */
	@Nonnull
	@CheckReturnValue
	DatabaseInsertionOrUpdate insertOrUpdate();

	/**
	 * @see Database#delete(String)
	 */
	@Nonnull
	@CheckReturnValue
	DatabaseDeletion delete();

	@Nonnull
	Database getParent();

}
