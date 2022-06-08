package cloud.hytora.modules.proxy.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TabList {

	/**
	 * A list of all frames in tab list
	 */
	private List<TabListFrame> frames;

	/**
	 * The time to switch the frame
	 */
	private double animationInterval;

}
