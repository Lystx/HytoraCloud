package cloud.hytora.driver.tps;

public interface TickCounter {

    int getSize();

    long getTime();

    double getTotal();

    int getIndex();

    double[] getSamples();

    long[] getTimes();

    double getAverage();

    void add(double x, long t);

}
