package cloud.hytora.driver.database.api.access;

import cloud.hytora.document.Document;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiFunction;

@AllArgsConstructor
@Getter
public class DirectDatabaseAccess<V> implements DatabaseAccess<V> {

	protected final Database database;
	protected final DatabaseAccessConfig config;
	protected final BiFunction<? super Document, ? super String, ? extends V> mapper;

	@Nullable
	@Override
	public V getValue(@Nonnull String key) throws DatabaseException {
		return getValue0(key).orElse(null);
	}

	@Nonnull
	@Override
	public V getValue(@Nonnull String key, @Nonnull V def) throws DatabaseException {
		return getValue0(key).orElse(def);
	}

	@Nonnull
	@Override
	public Optional<V> getValueOptional(@Nonnull String key) throws DatabaseException {
		return getValue0(key);
	}

	@Nonnull
	protected Optional<V> getValue0(@Nonnull String key) throws DatabaseException {
		return database.query(config.getTable())
				.where(config.getKeyField(), key)
				.execute().first()
				.map(document -> mapper.apply(document, config.getValueField()));
	}

	@Override
	public void setValue(@Nonnull String key, @Nullable V value) throws DatabaseException {
		database.insertOrUpdate(config.getTable())
				.set(config.getValueField(), value)
				.where(config.getKeyField(), key)
				.execute();
	}

	@Nonnull
	@Override
	public Database getDatabase() {
		return database;
	}

	@Nonnull
	@Override
	public DatabaseAccessConfig getConfig() {
		return config;
	}
}
