package cloud.hytora.modules.proxy.config.sub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter @AllArgsConstructor @NoArgsConstructor
public class Motd {

	/**
	 * A list of all default motds
	 * (when maintenance is off)
	 */
	private List<MotdLayOut> defaults;

	/**
	 * A list of all maintenance motds
	 * (when maintenance is on)
	 */
	private List<MotdLayOut> maintenances;

}
