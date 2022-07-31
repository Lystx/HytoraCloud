package cloud.hytora.node.console.progressbar;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class ProgressBar {

    /**
     * Checks if a progressBar is currently active
     */
    public static boolean isRunning() {
        return System.getProperty("progressbar.active", "false").equalsIgnoreCase("true");
    }

    /**
     * The left bracket that catches the process
     */
    private final String leftBracket;

    /**
     * The right bracket that catches the process
     */
    private final String rightBracket;

    /**
     * The cursor that steps within the two brackets and shows the process
     */
    private final String cursor;

    /**
     * The string that is appended after the cursor
     */
    private final String cursorEnd;

    /**
     * The time this bar was started
     */
    private final long startTime;

    /**
     * The total maxLength of this bar
     */
    private final long total;

    /**
     * The extra custom message that is appended at the end of the progress-line
     */
    private String extraMessage;

    /**
     * The name of this progress that is appended before the percentage of the progress
     */
    private String taskName;

    /**
     * The printer instance to print certain progress lines
     */
    private ProgressPrinter printer;

    /**
     * The current step of this progress
     */
    private long current;

    /**
     * The maximum percentage
     */
    private int percentagedWith;

    /**
     * If you have to execute {@link ProgressBar#print()}
     * manually or if it is being executed after calling {@link ProgressBar#step()}
     */
    private boolean printAutomatically;

    /**
     * If the bar expands (not recommended right now)
     */
    private boolean expandingAnimation;

    /**
     * Constructs a new {@link ProgressBar} with a pre-set {@link ProgressBarStyle}
     *
     * @param style     the style to use
     * @param maxLength the maximum length of the progress (like 100 for 100%)
     */
    public ProgressBar(ProgressBarStyle style, long maxLength) {
        this(style.getLeftBracket(), style.getRightBracket(), style.getCursor(), style.getCursorEnd(), maxLength);
    }

    /**
     * Constructs a new {@link ProgressBar} with a custom pre-set
     *
     * @param leftBracket  the left bracket that catches the progress
     * @param rightBracket the left bracket that catches the progress
     * @param cursor       the cursor that displays the progress
     * @param cursorEnd    the end that is being appended to the cursorEnd
     * @param maxLength    the maximum length of the progress (like 100 for 100%)
     */
    public ProgressBar(String leftBracket, String rightBracket, String cursor, String cursorEnd, long maxLength) {
        this.leftBracket = leftBracket;
        this.rightBracket = rightBracket;
        this.cursor = cursor;
        this.cursorEnd = cursorEnd;
        this.total = maxLength;
        this.percentagedWith = 100;

        this.startTime = System.currentTimeMillis();
        this.extraMessage = "";
        this.expandingAnimation = true;
        this.printer = new ProgressPrinter() {
            @Override
            public void print(String progress) {
                System.out.print(progress);
            }

            @Override
            public void flush(String progress) {
                System.out.println(progress);
            }
        };

        System.setProperty("progressbar.active", "true"); //used for api-purposes -> remember to close
    }

    /**
     * Sets the extra message and automatically prints the new message bar
     *
     * @param extraMessage the message
     */
    public void setExtraMessageAndUpdate(String extraMessage) {
        this.setExtraMessage(extraMessage);
        this.print();
    }

    /**
     * Steps one step up in the progress
     */
    public void step() {
        current++;
        this.stepTo(current);
    }

    /**
     * Steps to a given step in this progress
     *
     * @param n the number to step to
     */
    public void stepTo(long n) {
        this.current = n;

        if (printAutomatically) {
            print();
        }
    }

    /**
     * Prints the current progress
     */
    public void print() {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        long start = current == 0 ? 0 :
                (current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        String startHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(start),
                        TimeUnit.MILLISECONDS.toMinutes(start) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(start) % TimeUnit.MINUTES.toSeconds(1));

        String info = "(" + this.current + "/" + this.total + ", [" + startHms + " / " + etaHms + "])";

        int percent = (int) (current * percentagedWith / total);

        String string = "\r" + (taskName != null ? taskName : "") +
                String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), "")) +
                String.format(" %d%% " + leftBracket, (int) (current * 100 / total)) +
                (expandingAnimation ? String.join("", Collections.nCopies(percent, cursor)) : "") +
                cursorEnd +
                (expandingAnimation ? String.join("", Collections.nCopies(percentagedWith - percent, " ")) : "") +
                rightBracket +
                String.join("", Collections.nCopies(current == 0 ? (int) (Math.log10(total)) : (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")) +
                String.format(info + " " + this.extraMessage, current, total, etaHms);

        this.printer.print(string);

    }

    /**
     * Closes the current bar
     */
    public void close() {
        System.setProperty("progressbar.active", "false"); //used for api-purposes
        this.printer.flush("");
    }
}
