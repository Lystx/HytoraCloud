package cloud.hytora.driver.database.api.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@Getter
public class DatabaseConfig {

	private final String host;
	private final String database;
	private final String authDatabase;
	private final String password;
	private final String user;
	private String file;
	private final int port;
	private final boolean portIsSet;

	public DatabaseConfig(String host, String database, String password, String user, int port) {
		this(host, database, null, password, user, port, true, null);
	}
	public DatabaseConfig(String host, String database, String password, String user) {
		this(host, database, null, password, user, 0, false, null);
	}

	public DatabaseConfig(String host, String database, String authDatabase, String password, String user, int port) {
		this(host, database, authDatabase, password, user, port, true, null);
	}
	public DatabaseConfig(String host, String database, String authDatabase, String password, String user) {
		this(host, database, authDatabase, password, user, 0, false, null);
	}

	public DatabaseConfig(String database, String file) {
		this(null, database, null, null, null, 0, false, file);
	}

	public DatabaseConfig(String database, File file) {
		this(null, database, null, null, null, 0, false, file.toString());
	}

	public DatabaseConfig(String host, String database, String authDatabase, String password, String user, int port, boolean portIsSet, String file) {
		this.host = host;
		this.database = database;
		this.authDatabase = authDatabase;
		this.password = password;
		this.user = user;
		this.port = port;
		this.portIsSet = portIsSet;
		this.file = file;
	}

	public void setFile(File file) {
		this.file = file.toString();
	}

	@Override
	public String toString() {
		return "DatabaseConfig{" +
				"host='" + host + '\'' +
				", database='" + database + '\'' +
				", authDatabase='" + authDatabase + '\'' +
				", user='" + user + '\'' +
				", file='" + file + '\'' +
				", port=" + port +
				", portIsSet=" + portIsSet +
				'}';
	}

	public boolean isPortSet() {
		return portIsSet;
	}

	public File getFile() {
		return new File(file);
	}
}
