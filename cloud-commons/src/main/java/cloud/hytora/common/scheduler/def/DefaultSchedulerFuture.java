package cloud.hytora.common.scheduler.def;

import cloud.hytora.common.scheduler.SchedulerFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
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
	private List<Class<?>> ignoreExceptions = new ArrayList<>();

	@SafeVarargs
	@Override
	public final <T extends Throwable> void addIgnoreExceptionClass(Class<T>... exceptionClass) {
		ignoreExceptions.addAll(Arrays.asList(exceptionClass));
	}

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
			if (!this.ignoreExceptions.contains(e.getClass()) && this.ignoreExceptions.stream().noneMatch(c -> c.getClass().isAssignableFrom(e.getClass()))) {
				e.printStackTrace();
			}
		}
	}

}
