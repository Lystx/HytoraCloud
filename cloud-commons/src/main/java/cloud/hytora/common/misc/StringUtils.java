package cloud.hytora.common.misc;

import cloud.hytora.common.collection.WrappedException;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 1.2
 */
public final class StringUtils {

    private static final Pattern EMPTY_STRING_CHAIN = Pattern.compile("\\n+");

    public static final String SEPERATOR = "¶";
    public static final String SEPERATOR_2 = "Þ";
    public static final String SEPERATOR_3 = "þ";

    private StringUtils() {
    }
    /**
     * Gets a list to string with values and seperator.
     * Format: object $seperator object
     *
     * @param tList    The objects list
     * @param sep      The seperator (e.g. ';')
     * @param function The function
     * @param <T>      The type
     * @return The string
     */
    public static <T> String getListToString(Collection<T> tList, String sep, Function<T, String> function) {
        return String.join(sep, getStringList(tList, function));
    }

    public static <T> String getListToString(T[] tArray, String sep, Function<T, String> function) {
        return String.join(sep, getStringList(tArray, function));
    }

    /**
     * Gets a string list from given object list
     *
     * @param tList    The object list
     * @param function The function
     * @param <T>      The type
     * @return The string list
     */
    public static <T> List<String> getStringList(Collection<T> tList, Function<T, String> function) {
        List<String> l = new ArrayList<>();
        tList.forEach(t -> l.add(function.apply(t)));
        return l;
    }

    public static <T> List<String> getStringList(T[] tArray, Function<T, String> function) {
        List<String> l = new ArrayList<>();
        for(T t : tArray) {
            l.add(function.apply(t));
        }
        return l;
    }

    /**
     * Uppers the first letter
     *
     * @param str The string
     * @return The result
     */
    public static String upperFirstLetter(String str) {
        char[] stringArray = str.trim().toCharArray();
        stringArray[0] = Character.toUpperCase(stringArray[0]);
        return new String(stringArray);
    }
    /**
     * Uses google#Splitter to split a joined string
     *
     * @param seperator The seperator
     * @return The successful splitted string as stringList
     */
    public static List<String> split(String s, String seperator) {
        return Splitter.on(seperator).splitToList(s);
    }

    public static List<String> split(String s) {
        return split(s, SEPERATOR);
    }


    /**
     * Splits the string into an array. If you choose to keep the delimiters
     * all parts (delimiter or not) will be held inside an array until the end and
     * then returned as array
     *
     * @param str            The original string to split
     * @param regex          The regex determines the delimiter
     * @param keepDelimiters Should the delimiter be kept inside the split-array
     * @return The string array
     */
    public static String[] split(String str, String regex, boolean keepDelimiters) {
        if(!keepDelimiters) {
            return split(str, regex).toArray(new String[]{});
        }
        List<String> parts = new ArrayList<>();
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);

        int lastEnd = 0;
        while(m.find()){
            int start = m.start();
            if(lastEnd != start) {
                String nonDelim = str.substring(lastEnd, start);
                parts.add(nonDelim);
            }
            String delim = m.group();
            parts.add(delim);

            lastEnd = m.end();
        }
        if(lastEnd != str.length()) {
            String nonDelim = str.substring(lastEnd);
            parts.add(nonDelim);
        }
        return parts.toArray(new String[]{});
    }

    /**
     * Modifies a whole string list
     *
     * @param old      The old list to be edited
     * @param function The function to modify the strings
     * @return The list of string
     */
    public static List<String> modifyStringList(List<String> old, Function<String, String> function) {
        List<String> l = new ArrayList<>();
        old.forEach(s -> l.add(function.apply(s)));
        return l;
    }

    /**
     * Simple removes empty string from given list
     *
     * @param original The original string list
     * @return The result as list
     */
    public static List<String> removeEmpties(List<String> original) {
        List<String> l = new ArrayList<>(original);
        l.removeAll(Arrays.asList("", null));
        return l;
    }


    /**
     * Uses google#Splitter to split a joined string without empty results
     *
     * @param seperator The seperator
     * @return The successful splitted string as stringList
     */
    public static List<String> splitWithoutEmpty(String s, String seperator) {
        return Splitter.on(seperator).omitEmptyStrings().splitToList(s);
    }

    public static List<String> splitWithoutEmpty(String s) {
        return splitWithoutEmpty(s, SEPERATOR);
    }

    public static List<String> splitWithoutEmpty2(String s) {
        return splitWithoutEmpty(s, SEPERATOR_2);
    }


    /**
     * Uses google#Joiner to join given seperator into given objects
     *
     * @param seperator The seperator
     * @param objects   The objects
     * @return The successful as string
     */
    public static String join(String seperator, Object... objects) {
        List<String> l = new ArrayList<>();
        for(Object o : objects) {
            String s = "null";
            if(o != null) {
                s = o.toString();
            }
            l.add(s);
        }
        return Joiner.on(seperator).join(l);
    }



    /**
     * Get all similar strings from given list by comparing them with given string.
     *
     * @param string       The original string to compare
     * @param otherStrings The list of string to search for similarities
     * @param n            The n-gram size for the {@link #getProfile(String, int)} method
     * @param realistic    Realistic means at least the beginning letter is similar
     * @return The map of found string with similarity as double (Range: 0-1)
     */
    public static Map<String, Double> getSimilarities(String string, List<String> otherStrings, int n, boolean realistic) {
        Map<String, Double> similarities = new LinkedHashMap<>();

        for(String s : otherStrings) {
            if(s.isEmpty() || (realistic && !s.startsWith(string.substring(0, 1)))) {
                continue;
            }
            similarities.put(s, getSimilarity(s, string, n));
        }
        return Collections.unmodifiableMap(similarities);
    }

    /**
     * Uses the Jaccard algorithm to determine the similarity
     *
     * @param s1 The first string
     * @param s2 The second string
     * @param n  Size of the n-gram to check similarity
     * @return The Jaccard index between 0 and 1
     */
    public static double getSimilarity(String s1, String s2, int n) {
        if(s1.equals(s2)) {
            return 1;
        }
        Map<String, Integer> profile1 = getProfile(s1, n);
        Map<String, Integer> profile2 = getProfile(s2, n);

        Set<String> union = new HashSet<>();
        union.addAll(profile1.keySet());
        union.addAll(profile2.keySet());

        int inter = 0;

        for(String key : union) {
            if(profile1.containsKey(key) && profile2.containsKey(key)) {
                inter++;
            }
        }

        return 1.0 * inter / union.size();
    }


    /**
     * Splits given string into his profile as mentioned here: https://en.wikipedia.org/wiki/N-gram
     *
     * @param string The string
     * @param n      The size of the n-gram
     * @return The map of ngram and their occurence
     */
    public static Map<String, Integer> getProfile(String string, int n) {
        HashMap<String, Integer> ngrams = new HashMap<>();

        String withoutSpace = EMPTY_STRING_CHAIN.matcher(string).replaceAll(" ");
        for(int i = 0; i < (withoutSpace.length() - n + 1); i++) {
            String ngram = withoutSpace.substring(i, i + n);

            // increments occurence
            ngrams.merge(ngram, 1, (a, b) -> a + b);
        }
        return Collections.unmodifiableMap(ngrams);
    }


    /**
     * Get the most similar string out of given string list
     *
     * @param string       The string
     * @param otherStrings The string list
     * @param n            The n-gram
     * @param realistic    {@link #getSimilarities(String, List, int, boolean)}
     * @return The key of string and the similarity
     */
    public static Map.Entry<String, Double> getMostSimilar(String string, List<String> otherStrings, int n, boolean realistic) {
        Map<String, Double> similarities = getSimilarities(string, otherStrings, n, realistic);
        similarities = CollectionUtils.sortMapByValue(similarities, (o1, o2) -> o1.getValue().compareTo(o2.getValue()) * -1);

        Iterator<Map.Entry<String, Double>> iterator = similarities.entrySet().iterator();
        if(!iterator.hasNext()) return null;
        return iterator.next();
    }

    public static String getBetween(String input, String left, String right) {

        input = input.substring(input.indexOf(left) + 1);
        input = input.substring(0, input.indexOf(right));

        return input;
    }

    @Nonnull
    public static String getEnumName(@Nonnull Enum<?> enun) {
        return getEnumName(enun.name());
    }

    public static String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

    public static final Pattern SIMPLE_PLACEHOLER_REGEX = Pattern.compile("\\{[0-9]*}");
    public static final Pattern FORWARDING_PLACEHOLDER_REPLACE = Pattern.compile("\\([^\"]*\\)");
    public static final Pattern FORWARDING_PLACEHOLDER_REGEX = Pattern.compile("\\{[a-zA-Z\\-_]*}(" + FORWARDING_PLACEHOLDER_REPLACE + ")*");
    public static final Pattern DECIDING_PLACEHOLDER_REGEX = Pattern.compile("\\{\"[^\"]*\"\\|\"[^\"]*\"}");
    public static final Pattern KEY_PLACEHOLER_REGEX = Pattern.compile("%[a-zA-Z]*%");
    public static final Pattern REPLACE_REGEX = Pattern.compile(SIMPLE_PLACEHOLER_REGEX
            + "|" + KEY_PLACEHOLER_REGEX + "|" + FORWARDING_PLACEHOLDER_REGEX + "|" + DECIDING_PLACEHOLDER_REGEX);

    /**
     * Find matches in string from regex as filter
     *
     * @param regex  The regex
     * @param string The string to search in
     * @return The result as matches
     */
    public static List<String> find(Pattern regex, String string) {
        List<String> listMatches = new ArrayList<>();
        Matcher matcher = regex.matcher(string);

        while(matcher.find()){
            listMatches.add(matcher.group());
        }
        return listMatches;
    }

    public static List<String> find(String regex, String string) {
        return find(Pattern.compile(regex), string);
    }


    /* Formats given text by replacing all placeholders with given replacements<br>
     * If the placeholder contains a "abc-defg-hij" key then use the function
     * to search for a string which fits to this key.<br>
     * Can be used for {@link PropertiesConfig}
     *
             * @param text            The text
     * @param fetchUnknownKey The function if the placeholder contains a key for the property file
     * @param replacements    The replacements
     * @return The formatted string
     */
    public static String formatCustom(String text, Function<String, String> fetchUnknownKey, Object... replacements) {
        // if there are no replacements, just return the text
        if(replacements.length == 0) return text;

        // list all placeholders from the text inside a HashSet (no duplicates)
        // after getting the placeholders put them into a list (for sorting)
        // order them after this system: {0}, {1} first and then the others chronologically
        Set<String> placeHolderSet = new HashSet<>(find(REPLACE_REGEX, text));
        List<String> placeHolders = new ArrayList<>(placeHolderSet);

        placeHolders.sort((o1, o2) -> {
            boolean integerOrder1 = SIMPLE_PLACEHOLER_REGEX.matcher(o1).matches();
            boolean integerOrder2 = SIMPLE_PLACEHOLER_REGEX.matcher(o2).matches();

            // if both strings look like {x=number}
            if(integerOrder1 && integerOrder2) {
                return Integer.valueOf(o1.replaceAll("[{}]", ""))
                        .compareTo(Integer.valueOf(o2.replaceAll("[{}]", "")));
            }
            // if one string looks like {x=number}
            if(integerOrder1 || integerOrder2) {
                return integerOrder1 ? -1 : integerOrder2 ? 1 : 0;
            }
            return 0;
        });

        // get normal placeholders
        int replacementIndex;
        for(replacementIndex = 0; replacementIndex < placeHolders.size(); replacementIndex++) {
            String placeHolder = placeHolders.get(replacementIndex);
            Object replacement = replacementIndex >= replacements.length ? null : replacements[replacementIndex];

            if(replacement == null
                    || FORWARDING_PLACEHOLDER_REGEX.matcher(placeHolder).matches()
                    || DECIDING_PLACEHOLDER_REGEX.matcher(placeHolder).matches()) {
                continue;
            }
            text = text.replace(placeHolder, replacement + "");
        }

        // reload the text
        placeHolders = find(REPLACE_REGEX, text);

        // get placeholders and replace them
        for(int i = 0; i < placeHolders.size(); i++) {
            String placeHolder = placeHolders.get(i);
            Object replacement = i >= replacements.length ? null : replacements[i];

            // if the placeHolder contains an forwarding key
            if(FORWARDING_PLACEHOLDER_REGEX.matcher(placeHolder).matches()
                    && fetchUnknownKey != null) {
                List<Object> newReplacements = new ArrayList<>();
                if(replacement != null && replacement instanceof List) {
                    newReplacements.addAll((List) replacement);
                }
                for(String subReplacement : find(FORWARDING_PLACEHOLDER_REPLACE, placeHolder)) {
                    subReplacement = subReplacement.replaceAll("[()]", "");
                    newReplacements.add(subReplacement);
                }

                replacement = format(
                        fetchUnknownKey.apply(placeHolder.replaceAll("[{}]|" + FORWARDING_PLACEHOLDER_REPLACE, "")),
                        fetchUnknownKey,
                        newReplacements.toArray()
                );
            }

            // is the replacement null?
            if(replacement == null) continue;

            if(DECIDING_PLACEHOLDER_REGEX.matcher(placeHolder).matches()) {
                List<String> parts = find("\"[^\"]*\"", placeHolder);

                if(replacement instanceof Boolean) {
                    replacement = parts.get((Boolean) replacement ? 0 : 1).replaceAll("[\"]", "");
                }
                else if(replacement instanceof List) {
                    List l = (List) replacement;
                    Object key;

                    if(l.size() > 1 && ((key = l.get(0)) instanceof Boolean)) {
                        l = l.subList(1, l.size());
                        replacement = format(parts.get((Boolean) key ? 0 : 1).replaceAll("[\"]", ""), l.toArray());
                    }
                }
            }

            text = text.replace(placeHolder, replacement + "");
        }
        return text;
    }

    public static String formatCustom(String text, Object... replacements) {
        return formatCustom(text, s -> s, replacements);
    }

    public static String getReadableMillisDifference(long date1, long date2) {
        Map<TimeUnit, Long> diff = getMillisDifference(date1, date2);

        long days = diff.get(TimeUnit.DAYS);
        long hours = diff.get(TimeUnit.HOURS);
        long minutes = diff.get(TimeUnit.MINUTES);
        long seconds = diff.get(TimeUnit.SECONDS);

        if (days == 0) {
            if (hours == 0) {
                if (minutes == 0) {
                    return formatInt((int) seconds) + " sec";
                }
                return formatInt((int) minutes) + ":" + formatInt((int) seconds) + " min";
            } else {
                return formatInt((int) hours) + ":" + formatInt((int) minutes) + ":" + formatInt((int) seconds) + " h";
            }
        } else {
            return days + "days | " + hours + ":" + minutes + ":" + seconds + "h";
        }
    }


    private static String formatInt(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return String.valueOf(i);
    }

    public static Map<TimeUnit, Long> getMillisDifference(long date1, long date2) {

        long difference = date2 - date1;

        //create the list
        java.util.List<TimeUnit> units = new ArrayList<>(EnumSet.allOf(TimeUnit.class));
        Collections.reverse(units);

        //create the result map of TimeUnit and difference
        Map<TimeUnit, Long> result = new LinkedHashMap<>();
        long milliesRest = difference;

        for (TimeUnit unit : units) {

            //calculate difference in millisecond
            long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
            long diffInMilliesForUnit = unit.toMillis(diff);
            milliesRest = milliesRest - diffInMilliesForUnit;

            //put the result in the map
            result.put(unit, diff);
        }

        return result;
    }

    @Nonnull
    @CheckReturnValue
    public static String formatMessage(@Nullable Object messageObject, @Nonnull Object... args) {
        StringBuilder message = new StringBuilder(String.valueOf(messageObject));
        for (Object arg : args) {
            if (arg instanceof Throwable) {
                continue;
            }
            int index = message.indexOf("{}");
            if (index == -1) {
                break;
            }
            message.replace(index, index + 2, String.valueOf(arg));
        }
        return message.toString();
    }

    @Nonnull
    public static String getEnumName(@Nonnull String name) {
        StringBuilder builder = new StringBuilder();
        boolean nextUpperCase = true;
        for (char letter : name.toCharArray()) {
            // Replace _ with space
            if (letter == '_') {
                builder.append(' ');
                nextUpperCase = true;
                continue;
            }
            builder.append(nextUpperCase ? Character.toUpperCase(letter) : Character.toLowerCase(letter));
            nextUpperCase = false;
        }
        return builder.toString();
    }

    @Nonnull
    public static String format(@Nonnull String sequence, @Nonnull Object... args) {
        char start = '{', end = '}';
        boolean inArgument = false;
        StringBuilder argument = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        for (char c : sequence.toCharArray()) {
            if (c == end && inArgument) {
                inArgument = false;
                try {
                    int arg = Integer.parseInt(argument.toString());
                    Object current = args[arg];
                    Object replacement =
                            current instanceof Supplier ? ((Supplier<?>) current).get() :
                                    current instanceof Callable ? ((Callable<?>) current).call() :
                                            current;
                    builder.append(replacement);
                } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                    builder.append(start).append(argument).append(end);
                } catch (Exception ex) {
                    throw new WrappedException(ex);
                }
                argument = new StringBuilder();
                continue;
            }
            if (c == start && !inArgument) {
                inArgument = true;
                continue;
            }
            if (inArgument) {
                argument.append(c);
                continue;
            }
            builder.append(c);
        }
        if (argument.length() > 0) builder.append(start).append(argument);
        return builder.toString();
    }

    @Nonnull
    public static String getAfterLastIndex(@Nonnull String input, @Nonnull String separator) {
        return Optional.of(input)
                .filter(name -> name.contains(separator))
                .map(name -> name.substring(name.lastIndexOf(separator) + separator.length()))
                .orElse("");
    }

    @Nonnull
    public static String[] format(@Nonnull String[] array, @Nonnull Object... args) {
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = format(array[i], args);
        }
        return result;
    }

    @Nonnull
    public static String getArrayAsString(@Nonnull String[] array, @Nonnull String separator) {
        StringBuilder builder = new StringBuilder();
        for (String string : array) {
            if (builder.length() != 0) builder.append(separator);
            builder.append(string);
        }
        return builder.toString();
    }

    @Nonnull
    public static String[] getStringAsArray(@Nonnull String string) {
        return string.split("\n");
    }

    @Nonnull
    public static <T> String getIterableAsString(@Nonnull Iterable<T> iterable, @Nonnull String separator, @Nonnull Function<T, String> mapper) {
        StringBuilder builder = new StringBuilder();
        for (T t : iterable) {
            if (builder.length() > 0) builder.append(separator);
            String string = mapper.apply(t);
            builder.append(string);
        }
        return builder.toString();
    }

    @Nonnull
    public static String repeat(@Nullable Object sequence, int amount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < amount; i++) builder.append(sequence);
        return builder.toString();
    }

    private static int getMultiplier(char c) {
        switch (Character.toLowerCase(c)) {
            default:
                return 1;
            case 'm':
                return 60;
            case 'h':
                return 60 * 60;
            case 'd':
                return 24 * 60 * 60;
            case 'w':
                return 7 * 24 * 60 * 60;
            case 'y':
                return 365 * 24 * 60 * 60;
        }
    }

    public static long parseSeconds(@Nonnull String input) {
        if (input.toLowerCase().startsWith("perm")) return -1;
        long current = 0;
        long seconds = 0;
        for (char c : input.toCharArray()) {
            try {
                long i = Long.parseUnsignedLong(String.valueOf(c));
                current *= 10;
                current += i;
            } catch (Exception ignored) {
                int multiplier = getMultiplier(c);
                seconds += current * multiplier;
                current = 0;
            }
        }
        seconds += current;
        return seconds;
    }

    public static boolean isNumber(@Nonnull String sequence) {
        try {
            Double.parseDouble(sequence);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static int indexOf(@Nonnull String string, @Nonnull String pattern, int occurrenceIndex) {
        int lastIndex = 0;
        for (int currentLayer = 0; currentLayer <= occurrenceIndex; currentLayer++) {
            int index = string.indexOf(pattern, (lastIndex > 0) ? lastIndex + 1 : 0);
            if (index == -1) return -1;
            lastIndex = index + 1;
        }

        return lastIndex;
    }

}
