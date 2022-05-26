package cloud.hytora.driver.scheduler.def;

import cloud.hytora.driver.scheduler.SchedulerFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter @RequiredArgsConstructor @Setter
public class DefaultSchedulerFuture implements SchedulerFuture {

	private static final long serialVersionUID = 8617358757810936900L;
	//Constructor parameters
	private final boolean sync;
	private final Runnable runnable;
	private final int id;
	private final boolean repeating;

	//Non final fields
	private int runTimes;
	private boolean cancelled;
	private boolean error;

	private List<Consumer<SchedulerFuture>> taskConsumers = new ArrayList<>();
	private List<Supplier<Boolean>> cancelWhens = new ArrayList<>();

	@Override
	public SchedulerFuture addListener(Consumer<SchedulerFuture> consumer) {
		this.taskConsumers.add(consumer);
		return this;
	}

	@Override
	public SchedulerFuture cancelIf(Supplier<Boolean> booleanRequest) {
		this.cancelWhens.add(booleanRequest);
		return this;
	}

	@Override
	public void run() {
		if (cancelled || error) {
			return;
		}

		for (Supplier<Boolean> cancelWhen : this.cancelWhens) {
			if (cancelWhen.get()) {
				this.setCancelled(true);
			}
		}

		this.runTimes++;
		try {
			this.runnable.run();
			for (Consumer<SchedulerFuture> taskConsumer : this.taskConsumers) {
				taskConsumer.accept(this);
			}
		} catch (Exception e) {
			this.error = true;
			e.printStackTrace();
		}
	}

}
