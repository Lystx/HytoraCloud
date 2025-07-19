package cloud.hytora.common;

public class StopWatch {

    private long startTime;



    public void start() {
        this.startTime = System.currentTimeMillis();
    }



    public long stop() {
        return System.currentTimeMillis() - startTime;
    }
}
