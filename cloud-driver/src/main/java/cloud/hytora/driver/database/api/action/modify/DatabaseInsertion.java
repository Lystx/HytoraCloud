package cloud.hytora.driver.database.api.action.modify;

import cloud.hytora.document.Document;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.SpecificDatabase;
import cloud.hytora.driver.database.api.action.DatabaseAction;
import cloud.hytora.driver.database.api.action.hierarchy.SetAction;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 *
 * @see Database#insert(String)
 * @see SpecificDatabase#insert()
 */
public interface DatabaseInsertion extends DatabaseAction<Void>, SetAction {

	@Nonnull
	@CheckReturnValue
	DatabaseInsertion set(@Nonnull String field, @Nullable Object value);


	default DatabaseInsertion set(Document document) {
		DatabaseInsertion ins = null;
		Map<String, Object> values = document.toMap();
		for (String key : values.keySet()) {

			ins = this.set(key, values.get(key));
		}
		return ins;
	}

	@Nullable
	@Override
	Void execute() throws DatabaseException;

}
