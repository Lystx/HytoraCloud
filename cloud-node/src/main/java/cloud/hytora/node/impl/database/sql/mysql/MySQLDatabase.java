package cloud.hytora.node.impl.database.sql.mysql;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.api.impl.DatabaseConfig;
import cloud.hytora.driver.database.api.action.query.DatabaseListTables;
import cloud.hytora.node.impl.database.sql.abstraction.AbstractSQLDatabase;
import cloud.hytora.node.impl.database.sql.mysql.list.MySQLListTables;

import javax.annotation.Nonnull;

public class MySQLDatabase extends AbstractSQLDatabase {

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException ex) {
			CloudDriver.getInstance().getLogger().error("Could not load mysql driver");
		}
	}

	public MySQLDatabase(@Nonnull DatabaseConfig config) {
		super(config);
	}

	@Nonnull
	@Override
	protected String createUrl() {
		return "jdbc:mysql://" + config.getHost() + (config.isPortSet() ? ":" + config.getPort() : "") + "/" + config.getDatabase();
	}

	@Nonnull
	@Override
	public DatabaseListTables listTables() {
		return new MySQLListTables(this);
	}

}
