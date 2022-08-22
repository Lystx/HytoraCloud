package cloud.hytora.driver.tps;


public interface ICloudTickWorker {

    /**
     * The ticking time each tick takes
     */
    long getTickTime();

    /**
     * The seconds that are divided with
     */
    long getSecondsInNano();

    /**
     * The buffer for catching up to the next tick
     */
    long getMaxCatchupBuffer();

    /**
     * The maximum amount of ticks to measure
     * (Mostly 20 TPS)
     */
    int getMaxTps();

    /**
     * Sets the maximum amount of ticks
     *
     * @param maxTps the value to set
     */
    void setMaxTps(int maxTps);

    /**
     * Starts the ticking
     */
    void startTicking();

    /**
     * Gets a certain {@link TickCounter} depending on a
     * specified {@link TickType} (1min, 5min, 15min)
     *
     * @param type the type to provide
     * @return counter instance
     */
    TickCounter getTick(TickType type);

}
