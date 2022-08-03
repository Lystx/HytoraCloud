package cloud.hytora.driver.component.style;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.fusesource.jansi.Ansi;

@Getter @AllArgsConstructor
public enum ComponentColor  {

    /**
     * The reset color (Minecraft side) -> §r
     */
    RESET(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.DEFAULT).boldOff().toString(), 'f'),

    /**
     * The white color (Minecraft side) -> §f
     */
    WHITE(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString(), 'f'),

    /**
     * The black color (Minecraft side) -> §0
     */
    BLACK(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString(), '0'),
    
    /**
     * The red color (Minecraft side) -> §c
     */
    RED(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString(), 'c'),
    
    /**
     * The yellow color (Minecraft side) -> §e
     */
    YELLOW(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString(), 'e'),
    
    /**
     * The blue color (Minecraft side) -> §9
     */
    BLUE(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString(), '9'),
    
    /**
     * The green color (Minecraft side) -> §a
     */
    GREEN(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString(), 'a'),
    
    /**
     * The purple color (Minecraft side) -> §5
     */
    PURPLE(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString(), '5'),
    
    /**
     * The orange color (Minecraft side) -> §6
     */
    ORANGE(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString(), '6'),
    
    /**
     * The gray color (Minecraft side) -> §7
     */
    GRAY(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString(), '7'),
    
    /**
     * The dark red color (Minecraft side) -> §4
     */
    DARK_RED(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString(), '4'),
    
    /**
     * The dark gray color (Minecraft side) -> §8
     */
    DARK_GRAY(Ansi.ansi().reset().fg(Ansi.Color.BLACK).bold().toString(), '8'),
    
    /**
     * The dark blue color (Minecraft side) -> §1
     */
    DARK_BLUE(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString(), '1'),
    
    /**
     * The dark green color (Minecraft side) -> §2
     */
    DARK_GREEN(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString(), '2'),
    
    /**
     * The cyan color (Minecraft side) -> §3
     */
    LIGHT_BLUE(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString(), 'b'),
    
    /**
     * The aqua color (Minecraft side) -> §b
     */
    CYAN(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString(), '3'),

    /**
     * This is a console DARK_GRAY -> §h
     */
    EXTRA_GRAY(Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString(), 'h');

    /**
     * The color code to display in console
     */
    private final String code;

    /**
     * The minecraft color index
     */
    private final char index;

    @Override
    public String toString() {
        return code;
    }

    /**
     * Gets the minecraft color formatting
     * 
     * @return string with color index
     */
    public String getColor() {
        return "§" + index;
    }

    /**
     * Formats a String with Minecraft-Color-Codes and replaces them with
     * the correct {@link ComponentColor}
     *
     * @param c the char before the index
     * @param input the input string to format
     * @return formatted string
     */
    public static String translateColorCodes(char c, String input) {
        for (ComponentColor value : values()) {
            input = input.replace(String.valueOf(c) + value.getIndex(), value.toString());
        }
        return input + ComponentColor.RESET;
    }

    /**
     * Formats a String with Minecraft-Color-Codes and replaces them with
     * the correct {@link ComponentColor}
     *
     * @param c the char before the index
     * @param input the input string to format
     * @return formatted string
     */
    public static String translateAlternateColorCodes(char c, String input) {
        for (ComponentColor value : values()) {
            input = input.replace(String.valueOf(c) + value.getIndex(), "§" + value.getIndex());
        }
        return input;
    }

    /**
     * Removes all the Minecraft-Color-Codes out of a given {@link String}
     *
     * @param s the input to be replaced
     * @return the formatted string without any color codes
     */
    public static String replaceColorCodes(String s) {
        for (ComponentColor value : ComponentColor.values()) {
            s = s.replace("§" + value.getIndex(), "");
            s = s.replace("&" + value.getIndex(), "");
        }
        return s;
    }
}
