package cloud.hytora.modules.proxy.config.sub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter @AllArgsConstructor @NoArgsConstructor
public class MotdLayOut {

	/**
	 * The first line to display
	 */
	private String firstLine;

	/**
	 * The second line to display
	 */
	private String secondLine;

	/**
	 * The text that is where the player count is
	 */
	private String protocolText;

	/**
	 * The hover info text(s)
	 */
	private List<String> playerInfo;

}
