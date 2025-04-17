package cloud.hytora.node.impl.database.sql.sqlite;

import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.api.impl.DatabaseConfig;
import cloud.hytora.driver.database.api.action.query.DatabaseListTables;
import cloud.hytora.driver.database.api.exceptions.DatabaseException;
import cloud.hytora.node.impl.database.sql.abstraction.AbstractSQLDatabase;
import cloud.hytora.node.impl.database.sql.sqlite.list.SQLiteListTables;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

public class SQLiteDatabase extends AbstractSQLDatabase {

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException ex) {
			CloudDriver.getInstance().getLogger().error("Could not load sqlite driver");
		}
	}

	protected final File file;

	public SQLiteDatabase(@Nonnull DatabaseConfig config) {
		super(config);
		this.file = config.getFile();
	}

	@Override
	public void connect() throws DatabaseException {
		try {
			FileUtils.createFilesIfNecessary(file);
		} catch (IOException ex) {
			throw new DatabaseException(ex);
		}

		super.connect();
	}

	@Override
	protected String createUrl() {
		return "jdbc:sqlite:" + file;
	}

	@Nonnull
	@Override
	public DatabaseListTables listTables() {
		return new SQLiteListTables(this);
	}

}
