
package cloud.hytora.driver.tps.def;

import cloud.hytora.driver.tps.TickCounter;
import cloud.hytora.driver.tps.TickWorker;
import lombok.Getter;

@Getter
public class DefaultTickCounter implements TickCounter {

    private final int size;
    private long time;
    private double total;
    private int index = 0;
    private final double[] samples;
    private final long[] times;

    public DefaultTickCounter(TickWorker tickWorker, int size) {
        this.size = size;
        this.time = size * tickWorker.getSecondsInNano();
        this.total = tickWorker.getMaxTps() * tickWorker.getSecondsInNano() * size;
        this.samples = new double[size];
        this.times = new long[size];

        for (int i = 0; i < size; i++) {
            this.samples[i] = tickWorker.getMaxTps();
            this.times[i] = tickWorker.getSecondsInNano();
        }
    }

    @Override
    public void add(double x, long t) {
        this.time -= this.times[this.index];
        this.total -= this.samples[this.index] * this.times[this.index];
        this.samples[this.index] = x;
        this.times[this.index] = t;
        this.time += t;
        this.total += x * t;

        if (++this.index == this.size) {
            this.index = 0;
        }
    }

    @Override
    public double getAverage() {
        return this.total / this.time;
    }
}
