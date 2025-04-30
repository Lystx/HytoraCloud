package cloud.hytora.common.progressbar;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.misc.StringUtils;
import lombok.Getter;
import lombok.Setter;

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
    private long maxPercentage;

    /**
     * the length of the bar
     */
    private int barLength;

    /**
     * The extra custom message that is appended at the end of the progress-line
     */
    private String extraMessage;

    /**
     * The name of this progress that is appended before the percentage of the progress
     */
    private String taskName;

    private String spacer;

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

    private int calculatedStep;

    /**
     * Constructs a new {@link ProgressBar} with a pre-set {@link ProgressBarStyle}
     *
     * @param style         the style to use
     * @param maxPercentage the maximum length of the progress (like 100 for 100%)
     */
    public ProgressBar(ProgressBarStyle style, long maxPercentage) {
        this(style.getLeftBracket(), style.getRightBracket(), style.getCursor(), style.getCursorEnd(), style.getSpacer(), maxPercentage);
    }

    /**
     * Constructs a new {@link ProgressBar} with a custom pre-set
     *
     * @param leftBracket   the left bracket that catches the progress
     * @param rightBracket  the left bracket that catches the progress
     * @param cursor        the cursor that displays the progress
     * @param cursorEnd     the end that is being appended to the cursorEnd
     * @param maxPercentage the maximum length of the progress (like 100 for 100%)
     */
    public ProgressBar(String leftBracket, String rightBracket, String cursor, String cursorEnd, String spacer, long maxPercentage) {
        this.leftBracket = leftBracket;
        this.spacer = spacer;
        this.rightBracket = rightBracket;
        this.cursor = cursor;
        this.barLength = (int) maxPercentage;
        this.cursorEnd = cursorEnd;
        this.maxPercentage = maxPercentage;
        this.appendProgress = false;

        this.modifiedFakeDisplay = -1;
        this.calculatedStep = -1;

        this.startTime = System.currentTimeMillis();
        this.startDateTime = LocalDateTime.now();
        this.extraMessage = "";
        this.expandingAnimation = true;
        this.printer = new ProgressPrinter() {
            @Override
            public void print(String progress) {

                System.out.print(ConsoleColor.toColoredString('§', progress));
            }

            @Override
            public void flush(String progress) {
            }
        };

        System.setProperty("progressbar.active", "true"); //used for api-purposes -> remember to close
    }

    public void setAppendProgress() {
        this.appendProgress = true;
    }


    public void setSizeEntryBased(int size, int maxEntries) {
        int stepSize = size / maxEntries;

        this.calculatedStep = stepSize;

        this.barLength = (int) (stepSize * maxEntries);
    }

    /**
     * Steps one step up in the progress
     */
    public boolean step() {
        if (current >= maxPercentage) {
            return false;
        }
        if (calculatedStep != -1) {
            current = current + calculatedStep;
        } else {
            current++;
        }
        this.stepTo(current);
        return true;
    }

    public void setFakePercentage(int displayMax) {
        this.modifiedFakeDisplay = displayMax;
    }

    /**
     * Steps to a given step in this progress
     *
     * @param n the number to step to
     */
    public void stepTo(long n) {
        if (n > maxPercentage) {
            return;
        }
        this.current = n;

        if (printAutomatically) {
            print();
        }
    }

    /**
     * Steps to a given step in this progress
     *
     * @param n the number to step to
     */
    public void addStep(long n) {
        if ((this.current + n) > maxPercentage) {
            return;
        }
        this.current = this.current + n;

        if (printAutomatically) {
            print();
        }
    }

    /**
     * Prints the current progress
     */
    public void print() {

        long eta = current == 0 ? 0 :
                (maxPercentage - current) * (System.currentTimeMillis() - startTime) / current;

        long start = current == 0 ? 0 :
                (current) * (System.currentTimeMillis() - startTime) / current;


        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        int percent = (int) (current * 100 / maxPercentage);


        StringBuilder cursorProcess = new StringBuilder();

        //cursor

        for (int i = 0; i <  percent; i++) {
            cursorProcess.append(cursor);
        }


        int borderSpaces = current == 0 ? (int) (Math.log10(barLength)) : (int) (Math.log10(barLength)) - (int) (Math.log10(current));


        int x = barLength - percent;

        if (x < 0) {
            x = 0;
        }

        String string = (this.appendProgress ? "" : "\r")  +
                "§8» §7" +
                String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")) +
                String.format(" %d%% " + leftBracket, ((percent))) +
                (expandingAnimation ? cursorProcess.toString() : cursor) +
                cursorEnd +
                String.join("", Collections.nCopies(x, spacer)) +
                rightBracket +
                String.join("", Collections.nCopies(borderSpaces, " ")) +
                " §8[" + (taskName != null ? ("§8=> §7" + taskName + " §8| ") : "") + "§eETA§8: §e" + etaHms + "§8]";


        this.printer.print(string);
    }

    /**
     * Closes the current bar
     */
    public void close(String message, Object... args) {
        System.setProperty("progressbar.active", "false"); //used for api-purposes

        this.printer.print(StringUtils.formatMessage(message, args));
        this.printer.print("\n");
        this.printer.flush("");
    }
}
