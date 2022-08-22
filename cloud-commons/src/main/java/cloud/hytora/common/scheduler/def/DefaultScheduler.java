package cloud.hytora.common.scheduler.def;


import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.scheduler.SchedulerFuture;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Getter
public class DefaultScheduler implements Scheduler {

	/**
	 * All pending tasks
	 */
	private final List<SchedulerFuture> tasks;

	/**
	 * The java timer util
	 */
	private final Timer timer;

	/**
	 * The java executor service
	 */
	protected final ScheduledExecutorService scheduledExecutor;

	public static final DefaultScheduler INSTANCE = new DefaultScheduler();

	public DefaultScheduler() {
		this.tasks = new ArrayList<>();
		this.timer = new Timer("Scheduler");
		this.scheduledExecutor = Executors.newScheduledThreadPool(4, new NamedThreadFactory("Scheduler"));
	}

	@Override
	public SchedulerFuture getTask(int id) {
		return new LinkedList<>(this.tasks).stream().filter(task -> task.getId() == id).findFirst().orElse(null);
	}

	@Override @Deprecated
	public void cancelTask(int id) {
		this.cancelTask(this.getTask(id));
	}

	@Override
	public void cancelTask(SchedulerFuture task) {
		if (task != null) {
			task.setCancelled(true);
			this.tasks.removeIf(task1 -> task1 != null && task.getId() == task1.getId());
		}
	}


	@Override
	public void executeIf(Runnable runnable, Supplier<Boolean> request, long timeOut) {
		this.scheduledExecutor.execute(() -> {
			long deadline = System.currentTimeMillis() + timeOut;
			boolean done;

			do {
				done = request.get();
				if (!done) {
					long msRemaining = deadline - System.currentTimeMillis();
					if (msRemaining < 0) {
						done = true;
					}
				} else {
					runnable.run();
				}
			} while (!done);
		});
	}

	@Override
	public void executeIf(Runnable runnable, Supplier<Boolean> request) {
		this.executeIf(runnable, request, TimeUnit.DAYS.toMillis(1));
	}


	@Override
	public void cancelAllTasks() {
		for (SchedulerFuture task : this.getTasks()) {
			this.cancelTask(task);
		}
	}

	@Override
	public SchedulerFuture scheduleRepeatingTaskForTimes(Runnable task, long delay, long period, long times) {
		return scheduleRepeatingTaskForTimes(task, delay, period, times, false);
	}

	@Override
	public SchedulerFuture scheduleRepeatingTaskAsync(Runnable task, long delay, long period, long times) {
		return scheduleRepeatingTaskForTimes(task, delay, period, times, true);
	}

	@Override
	public SchedulerFuture scheduleRepeatingTask(Runnable task, long delay, long period) {
		return repeatTask(task, delay, period, false);
	}

	@Override
	public SchedulerFuture scheduleRepeatingTaskAsync(Runnable task, long delay, long period) {
		return repeatTask(task, delay, period, true);
	}

	@Override
	public SchedulerFuture runTask(Runnable task) {
		DefaultSchedulerFuture defaultSchedulerFuture = this.runTask(task, false, false);

		new Thread(() -> {
			defaultSchedulerFuture.run();
			cancelTask(defaultSchedulerFuture);
			Thread.interrupted();
		}, "scheduledTask_" + defaultSchedulerFuture.getId()).start();

		return defaultSchedulerFuture;
	}

	@Override
	public SchedulerFuture runTaskAsync(Runnable task) {
		DefaultSchedulerFuture defaultSchedulerFuture = runTask(task, true, false);

		new Thread(() -> {
			defaultSchedulerFuture.run();
			cancelTask(defaultSchedulerFuture);
			Thread.interrupted();
		}, "scheduledTask_" + defaultSchedulerFuture.getId()).start();

		return defaultSchedulerFuture;
	}

	@Override
	public SchedulerFuture scheduleDelayedTask(Runnable task, long delay) {
		return delayTask(task, delay, false);
	}

	@Override
	public SchedulerFuture scheduleDelayedTaskAsync(Runnable task, long delay) {
		return delayTask(task, delay, true);
	}

	//Helper method to internally run a task
	private DefaultSchedulerFuture runTask(Runnable task, boolean async, boolean multipleTimes) {
		if (task == null) {
			return null;
		}
		DefaultSchedulerFuture defaultSchedulerFuture = new DefaultSchedulerFuture(!async, task, generateTaskId(), multipleTimes);
		this.tasks.add(defaultSchedulerFuture);
		return defaultSchedulerFuture;
	}

	//Internal helper method to delay task
	public SchedulerFuture delayTask(Runnable task, long delay, boolean async) {

		DefaultSchedulerFuture defaultSchedulerFuture = runTask(task, async, false);

		this.timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				defaultSchedulerFuture.run();
				cancelTask(defaultSchedulerFuture);
				cancel();
				Thread.interrupted();
			}
		}, delay, 1);

		return defaultSchedulerFuture;
	}

	//Helper method to repeat tasks
	private SchedulerFuture repeatTask(Runnable task, long delay, long period, boolean async) {
		DefaultSchedulerFuture defaultSchedulerFuture = runTask(task, async, true);

		this.timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
					defaultSchedulerFuture.run();
					if (defaultSchedulerFuture.isCancelled()) {
						cancelTask(defaultSchedulerFuture);
						cancel();
						Thread.interrupted();
					}
			}
		}, delay, period);
		return defaultSchedulerFuture;
	}

	//Helper method to repeat task for times
	private SchedulerFuture scheduleRepeatingTaskForTimes(Runnable task, long delay, long period, final long times, boolean async) {
		DefaultSchedulerFuture defaultSchedulerFuture = runTask(task, async, true);

		this.timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
					defaultSchedulerFuture.run();
					if (defaultSchedulerFuture.isCancelled() || defaultSchedulerFuture.getRunTimes() >= times) {
						cancelTask(defaultSchedulerFuture);
						cancel();
						Thread.interrupted();
					}
			}
		}, delay, period);
		return defaultSchedulerFuture;
	}

	@Override
	public int generateTaskId() {
		int id = ThreadLocalRandom.current().nextInt();
		if (this.getTask(id) != null) {
			return generateTaskId();
		}
		return id;
	}

}