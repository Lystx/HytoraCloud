package cloud.hytora.driver.database.api.impl;

import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.SpecificDatabase;
import cloud.hytora.driver.database.api.action.modify.DatabaseDeletion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertion;
import cloud.hytora.driver.database.api.action.modify.DatabaseInsertionOrUpdate;
import cloud.hytora.driver.database.api.action.modify.DatabaseUpdate;
import cloud.hytora.driver.database.api.action.query.DatabaseCountEntries;
import cloud.hytora.driver.database.api.action.query.DatabaseQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class DefaultSpecificDatabase implements SpecificDatabase {

	protected final Database parent;
	protected final String name;

	@Override
	public boolean isConnected() {
		return parent.isConnected();
	}

	@Nonnull
	@Override
	public DatabaseCountEntries countEntries() {
		return parent.countEntries(name);
	}

	@Nonnull
	@Override
	public DatabaseQuery query() {
		return parent.query(name);
	}

	@Nonnull
	@Override
	public DatabaseUpdate update() {
		return parent.update(name);
	}

	@Nonnull
	@Override
	public DatabaseInsertion insert() {
		return parent.insert(name);
	}

	@Nonnull
	@Override
	public DatabaseInsertionOrUpdate insertOrUpdate() {
		return parent.insertOrUpdate(name);
	}

	@Nonnull
	@Override
	public DatabaseDeletion delete() {
		return parent.delete(name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DefaultSpecificDatabase that = (DefaultSpecificDatabase) o;
		return Objects.equals(parent, that.parent) && Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parent, name);
	}
}
