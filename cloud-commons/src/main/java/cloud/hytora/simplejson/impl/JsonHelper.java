package cloud.hytora.simplejson.impl;


import java.util.regex.Pattern;

public class JsonHelper {

    public static final int MIN_BUFFER_SIZE = 10;
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    public static final Pattern NEED_ESCAPE_NAME = Pattern.compile("[,\\{\\[\\}\\]\\s:#\"']|//|/\\*");

    public static boolean isPunctuatedChar(int c) {
        return c == '{' || c == '}' || c == '[' || c == ']' || c == ',' || c == ':';
    }

    public static boolean isWhiteSpace(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
    }

    /**
     * Checks if the provided text starts with something
     *
     * @param text the text
     * @return boolean value (yes/no)
     */
    public static boolean startsWith(String text) {
        int p;
        if (text.startsWith("true") || text.startsWith("null")) {
            p = 4;
        } else if (text.startsWith("false")) {
            p = 5;
        } else {
            return false;
        }
        while (p < text.length() && isWhiteSpace(text.charAt(p))) {
            p++;
        }
        if (p == text.length()) {
            return true;
        }
        char ch = text.charAt(p);
        return ch == ',' || ch == '}' || ch == ']' || ch == '#' || ch == '/' && (text.length() > p + 1 && (text.charAt(p + 1) == '/' || text.charAt(p + 1) == '*'));
    }
    /**
     * Gets the Wrapper-class of a primitive class
     *
     * @param primitiveClass the primitive class
     * @return the class
     */
    public static Class<?> getWrapperClassForPrimitive(Class<?> primitiveClass) {
        if (primitiveClass == boolean.class) {
            return Boolean.class;
        } else if (primitiveClass == int.class) {
            return Integer.class;
        } else if (primitiveClass == double.class) {
            return Double.class;
        } else if (primitiveClass == byte.class) {
            return Byte.class;
        } else if (primitiveClass == short.class) {
            return Short.class;
        } else if (primitiveClass == float.class) {
            return Float.class;
        } else if (primitiveClass == long.class) {
            return Long.class;
        } else if (primitiveClass == char.class) {
            return Character.class;
        } else {
            return primitiveClass;
        }
    }


}
