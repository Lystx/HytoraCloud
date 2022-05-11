package cloud.hytora.node.console.progressbar;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public enum ProgressBarStyle {

    /**
     * A simple style using ascii (no color or utf-8 symbols)
     */
    ASCII("[", "]", "=", ">"),

    /**
     * A nice style using utf-8 symbols (no colors)
     */
    UNICODE_BLOCK("│", "│", "█", ""),

    /**
     * This is in testing
     */
    HORIZONTAL_LINE("\u001b[0m[", "\u001b[0m]", "\u001b[33m|", ""),

    /**
     * A nice style using utf-8 symbols and colors aswell
     */
    COLORED_UNICODE_BLOCK("\u001b[33m│", "│\u001b[0m", "█", "");

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
}
