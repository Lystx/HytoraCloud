package cloud.hytora.common.progressbar;

public interface ProgressPrinter {

    /**
     * Prints progress from a {@link ProgressBar}
     *
     * @param progress the line to print to e.g console
     */
    void print(String progress);

    default void flush() {}
}
