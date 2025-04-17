package cloud.hytora.driver.database.api.access;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class DatabaseAccessConfig {

	private final String table;
	private final String keyField;
	private final String valueField;

}
