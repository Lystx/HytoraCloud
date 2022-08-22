
package cloud.hytora.driver.tps.def;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.tps.TickCounter;
import cloud.hytora.driver.tps.TickType;
import cloud.hytora.driver.tps.ICloudTickWorker;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class DefaultTickWorker implements ICloudTickWorker {

    /**
     * All registered tick counters
     */
    private final Map<TickType, TickCounter> tickCounters;

    /**
     * The current tick index
     */
    private long currentTick;

    public DefaultTickWorker(int maxTps) {
        this.tickCounters = new ConcurrentHashMap<>();
        this.currentTick = 0;

        //setting max tps to parameter tps
        this.setMaxTps(maxTps);

        //inserting all default tick-types
        for (TickType value : TickType.values()) {
            this.tickCounters.put(value, new DefaultTickCounter(this, value.getSize()));
        }

        //start ticking
        new Thread(this::startTicking, "tps-thread").start();
    }

    //values depending on maximum tps amount
    private int maxTps;
    private long tickTime;
    private long maxCatchupBuffer;

    @Override
    public void setMaxTps(int maxTps) {
        this.maxTps = maxTps;
        this.tickTime = getSecondsInNano() / maxTps;
        this.maxCatchupBuffer = this.tickTime * maxTps * 60L;
    }

    @Override
    public long getSecondsInNano() {
        return 1_000_000_000;
    }

    @Override
    public void startTicking() {

        long start = System.nanoTime();
        long lastTick = start - tickTime;
        long catchupTime = 0;
        long curTime;
        long wait;
        long tickSection = start;

        while (CloudDriver.getInstance().isRunning()) {
            try {
                curTime = System.nanoTime();
                wait = tickTime - (curTime - lastTick);

                if (wait > 0) {
                    if (catchupTime < 2E6) {
                        wait += Math.abs(catchupTime);
                    }

                    if (wait < catchupTime) {
                        catchupTime -= wait;
                        wait = 0;
                    } else if (catchupTime > 2E6) {
                        wait -= catchupTime;
                        catchupTime -= catchupTime;
                    }
                }

                if (wait > 0) {
                    Thread.sleep(wait / getSecondsInNano());
                    wait = tickTime - (curTime - lastTick);
                }

                catchupTime = Math.min(maxCatchupBuffer, catchupTime - wait);
                if (currentTick++ % maxTps == 0) {
                    long diff = curTime - tickSection;
                    double currentTps = 1E9 / diff * maxTps;

                    //adding a tick to every counter that is registered
                    for (TickType tickType : this.tickCounters.keySet()) {
                        TickCounter tickCounter = this.tickCounters.get(tickType);
                        tickCounter.add(currentTps, diff);
                        this.tickCounters.put(tickType, tickCounter);
                    }

                    tickSection = curTime;
                }

                lastTick = curTime;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public TickCounter getTick(TickType type) {
        return tickCounters.get(type);
    }

}
