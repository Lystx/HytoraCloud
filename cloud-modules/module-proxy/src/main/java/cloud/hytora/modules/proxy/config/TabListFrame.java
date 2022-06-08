package cloud.hytora.modules.proxy.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter @AllArgsConstructor @NoArgsConstructor
public class TabListFrame {

	/**
	 * The header of this tab list
	 */
	private String header;

	/**
	 * The footer of this tab list
	 */
	private String footer;

}
