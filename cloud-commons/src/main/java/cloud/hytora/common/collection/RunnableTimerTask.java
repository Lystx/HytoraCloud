package cloud.hytora.common.collection;

import javax.annotation.Nonnull;
import java.util.TimerTask;

/**

 * @since 1.2
 */
public class RunnableTimerTask extends TimerTask {

	protected final Runnable action;

	public RunnableTimerTask(@Nonnull Runnable action) {
		this.action = action;
	}

	@Override
	public void run() {
		action.run();
	}

}
