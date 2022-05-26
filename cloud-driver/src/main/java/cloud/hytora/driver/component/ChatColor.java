
package cloud.hytora.driver.component;

import lombok.Getter;

import java.util.Arrays;
import java.util.function.Supplier;


/**
 * Represents the different colors that can be sent in chat.
 *
 * @author Lystx
 * @since 1.0
 */
public enum ChatColor {

    /**
     * Black, &#167;0
     */
    BLACK('0'),

    /**
     * Dark Blue, &#167;1
     */
    DARK_BLUE('1'),

    /**
     * Dark Green, &#167;2
     */
    DARK_GREEN('2'),

    /**
     * Dark Aqua, &#167;3
     */
    DARK_AQUA('3'),

    /**
     * Dark Red, &#167;4
     */
    DARK_RED('4'),

    /**
     * Dark Purple, &#167;5
     */
    DARK_PURPLE('5'),

    /**
     * Gold, &#167;6
     */
    GOLD('6'),

    /**
     * Gray, &#167;7
     */
    GRAY('7'),

    /**
     * Gray, &#167;8
     */
    DARK_GRAY('8'),

    /**
     * Blue, &#167;9
     */
    BLUE('9'),

    /**
     * Green, &#167;a
     */
    GREEN('a'),

    /**
     * Aqua, &#167;b
     */
    AQUA('b'),

    /**
     * Red, &#167;c
     */
    RED('c'),

    /**
     * Light Purple, &#167;d
     */
    LIGHT_PURPLE('d'),

    /**
     * Yellow, &#167;e
     */
    YELLOW('e'),

    /**
     * White, &#167;f
     */
    WHITE('f'),

    /**
     * Obfuscated, &#167;k
     */
    OBFUSCATED('k'),

    /**
     * Bold, &#167;l
     */
    BOLD('l'),

    /**
     * Strikethrough, &#167;m
     */
    STRIKETHROUGH('m'),

    /**
     * Underline, &#167;n
     */
    UNDERLINE('n'),

    /**
     * Italic, &#167;o
     */
    ITALIC('o'),

    /**
     * Reset, &#167;r
     */
    RESET('r');

    /**
     * The char index of this color
     */
    @Getter
    private final char colorChar;

    @Getter
    public static final char escape = '\u00A7';

    /**
     * Creates a new char color based on the given character
     * which represents the canonical control sequence
     * for that particular color.
     *
     * @param colorChar the color character
     */
    ChatColor(char colorChar) {
        this.colorChar = colorChar;
    }

    /**
     * Gets if this chat color is a format color.
     *
     * @return True iff it is.
     */
    public boolean isFormat() {
        return 'k' <= this.colorChar && this.colorChar <= 'r';
    }

    /**
     * Gets if this chat color is a color.
     *
     * @return The color.
     */
    public boolean isColor() {
        return !this.isFormat();
    }

    /**
     * Gets a string representation of this color, in the
     * form &#167;{@code x}, where x is the color's
     * character.
     *
     * @return The color.
     */
    @Override
    public String toString() {
        return String.valueOf('\u00A7') + this.colorChar;
    }

    public String format() {
        return "§" + this.colorChar;
    }

    /**
     * Transforms all possible color codes in a given text and formats all
     * text-sequences that contain color codes to a colored string
     *
     * @param altColorChar the char that should be before the index (e.g. §2)
     * @param textToTranslate the text to translate (e.g. "§aHello world§7!")
     * @return the text with all replaced color codes
     */
    public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    /**
     * Gets a chat color from a given character.
     *
     * @param colorChar The color's character.
     * @return The color, or null if not found.
     */
    public static ChatColor of(String colorChar) {
        try {
            return Arrays.stream(values()).filter(chatColor -> String.valueOf(chatColor.colorChar).equalsIgnoreCase(colorChar)).findFirst().orElseThrow((Supplier<Throwable>) () -> new IllegalArgumentException("no color with character " + colorChar));
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * Gets a chat color from a given character.
     *
     * @param colorChar The color's character.
     * @return The color, or null if not found.
     */
    public static ChatColor of(char colorChar) {
        return of(String.valueOf(colorChar));
    }
}
