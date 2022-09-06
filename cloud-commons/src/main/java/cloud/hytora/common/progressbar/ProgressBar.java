package cloud.hytora.common.progressbar;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.misc.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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

    private LocalDateTime startDateTime;

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
     * If \r should not be in front of message
     */
    private boolean appendProgress;

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
     * The fake stats
     */
    private int modifiedFakeDisplay;

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
        this.appendProgress = false;

        this.modifiedFakeDisplay = -1;

        this.startTime = System.currentTimeMillis();
        this.startDateTime = LocalDateTime.now();
        this.extraMessage = "";
        this.expandingAnimation = true;
        this.printer = System.out::print;

        System.setProperty("progressbar.active", "true"); //used for api-purposes -> remember to close
    }

    public void setAppendProgress() {
        this.appendProgress = true;
    }

    /**
     * Steps one step up in the progress
     */
    public void step() {
        current++;
        this.stepTo(current);
    }

    public void setFakePercentage(int actualMax, int displayMax) {
        this.modifiedFakeDisplay = displayMax;

        this.percentagedWith = actualMax;
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

        int finalTotal = this.modifiedFakeDisplay == -1 ? (int) this.total : this.modifiedFakeDisplay;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        String startHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(start),
                        TimeUnit.MILLISECONDS.toMinutes(start) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(start) % TimeUnit.MINUTES.toSeconds(1));

        String info = "(" + this.current + "/" + finalTotal + ", [" + startHms + " / " + etaHms + "])";

        int percent = (int) (current * percentagedWith / finalTotal);


        StringBuilder cursorProcess = new StringBuilder();
        StringBuilder emptyProcess = new StringBuilder();
        StringBuilder borderProcess = new StringBuilder();

        //cursor
        for (int i = 0; i < percent; i++) {
            cursorProcess.append(cursor);
        }

        int emptySpaces = current == 0 ? (int) (Math.log10(total)) : (int) (Math.log10(total)) - (int) (Math.log10(current));
        int borderSpaces = current == 0 ? (int) (Math.log10(total)) : (int) (Math.log10(total)) - (int) (Math.log10(current));

        //empty
        for (int i = 0; i < emptySpaces; i++) {
            emptyProcess.append(" ");
        }

        //border
        for (int i = 0; i < borderSpaces; i++) {
            borderProcess.append(" ");
        }

        String string = (this.appendProgress ? "" : "\r") + (taskName != null ? (taskName + ConsoleColor.DEFAULT) : "") +
                String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), "")) +
                String.format(" %d%% " + leftBracket, (int) (current * 100 / total)) +
                (expandingAnimation ? cursorProcess.toString() : cursor) +
                cursorEnd +
                (expandingAnimation ? emptyProcess.toString() : "") +
                rightBracket +
                borderProcess.toString() +
                String.format(info + " " + this.extraMessage, current, finalTotal, etaHms);

        this.printer.print(string);

    }

    /**
     * Closes the current bar
     */
    public void close(String message, Object... args) {
        System.setProperty("progressbar.active", "false"); //used for api-purposes

        this.printer.print(StringUtils.formatMessage(message, args));
        this.printer.print("\n");
        this.printer.flush();
    }
}
