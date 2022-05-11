package cloud.hytora.node.console.progressbar;

public interface ProgressPrinter {

    /**
     * Prints progress from a {@link ProgressBar}
     *
     * @param progress the line to print to e.g console
     */
    void print(String progress);

    void flush(String progress);
}
