package cloud.hytora.common.misc;

import cloud.hytora.common.logging.Logger;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

/**

 * @since 1.2
 */
public final class CollectionUtils {

	@Deprecated
	public static final String REGEX_1 = ",";

	@Deprecated
	public static final String REGEX_2 = "=";

	private static boolean logMappingError = true;

	private CollectionUtils() {}

	public static void disableErrorLogging() {
		logMappingError = false;
	}

	/**
	 * You should not use this to serialize maps due to it's unsafe because of strings which may contain the regex chars = or ,
	 * Use better and safer serialization strategies like json.
	 *
	 * @deprecated Unsafe because of strings containing , or =
	 */
	@Nonnull
	@Deprecated
	@CheckReturnValue
	public static <K, V> String convertMapToString(@Nonnull Map<K, V> map, @Nonnull Function<K, String> key, @Nonnull Function<V, String> value) {
		StringBuilder builder = new StringBuilder();
		for (Entry<K, V> entry : map.entrySet()) {
			if (builder.length() != 0) builder.append(REGEX_1);
			builder.append(key.apply(entry.getKey()));
			builder.append(REGEX_2);
			builder.append(value.apply(entry.getValue()));
		}
		return builder.toString();
	}

	@SafeVarargs
	public static <T> Map<T, T> mapOf(T... keysAndValues) {
		Map<T, T> map = new HashMap<>();

		if (keysAndValues.length % 2 != 0)
			throw new IllegalArgumentException("Cannot create document of " + keysAndValues.length + " arguments");
		for (int i = 0; i < keysAndValues.length; i += 2) {
			map.put(keysAndValues[i], keysAndValues[i + 1]);
		}

		return map;
	}


	@Nonnull
	@CheckReturnValue
	public static String localhost() {
		try {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("https://checkip.amazonaws.com").openStream()))) {
				return reader.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
		}

		return "127.0.0.1";
	}
	/**
	 * You should not use this to serialize maps due to it's unsafe because of strings which may contain the regex chars = or ,
	 * Use better and safer serialization strategies like json.
	 *
	 * @deprecated Unsafe because of strings containing , or =
	 */
	@Nonnull
	@Deprecated
	@CheckReturnValue
	public static <K, V> Map<K, V> convertStringToMap(@Nullable String string, @Nonnull Function<String, K> key, @Nonnull Function<String, V> value) {

		Map<K, V> map = new HashMap<>();
		if (string == null) return map;

		String[] args = string.split(REGEX_1);
		for (String arg : args) {
			try {

				String[] elements = arg.split(REGEX_2);
				K keyElement = key.apply(elements[0]);
				V valueElement = value.apply(elements[1]);

				if (keyElement == null || valueElement == null)
					throw new NullPointerException();

				map.put(keyElement, valueElement);

			} catch (Exception ex) {
				//if (logMappingError)
					//logger.error("Cannot generate key/value: " + ex.getClass().getName() + ": " + ex.getMessage());
			}
		}

		return map;

	}

	public static <T> void updateValue(List<? super T> collection, T oldValue, T newValue) {
		int index = collection.indexOf(oldValue);
		collection.set(index, newValue);
	}

	@Nonnull
	@CheckReturnValue
	public static <FromK, FromV, ToK, ToV> Map<ToK, ToV> convertMap(@Nonnull Map<FromK, FromV> map,
	                                                                @Nonnull Function<? super FromK, ? extends ToK> keyMapper,
	                                                                @Nonnull Function<? super FromV, ? extends ToV> valueMapper) {
		Map<ToK, ToV> result = new HashMap<>();
		map.forEach((key, value) -> {
			try {
				result.put(keyMapper.apply(key), valueMapper.apply(value));
			} catch (Exception ex) {
				//if (logMappingError)
					//logger.error("Unable to map '{}'='{}'", key, value, ex);
			}
		});
		return result;
	}

	@Nonnull
	@CheckReturnValue
	public static <From, To> List<To> convertCollection(@Nonnull Collection<From> collection,
	                                                    @Nonnull Function<? super From, ? extends To> mapper) {
		List<To> result = new ArrayList<>(collection.size());
		collection.forEach(value -> {
			try {
				result.add(mapper.apply(value));
			} catch (Exception ex) {
				//if (logMappingError)
					//logger.error("Unable map '{}'", value, ex);
			}
		});
		return result;
	}

	@Nonnull
	@CheckReturnValue
	public static <From, To> List<To> convertIterator(@Nonnull Iterator<From> iterator,
	                                                    @Nonnull Function<? super From, ? extends To> mapper) {
		List<To> result = new ArrayList<>();
		while (iterator.hasNext()) {
			From value = iterator.next();
			try {
				result.add(mapper.apply(value));
			} catch (Exception ex) {
				//if (logMappingError)
					//logger.error("Unable map '{}'", value, ex);
			}
		}
		return result;
	}

	@Nonnull
	@CheckReturnValue
	public static <FromK, FromV, ToK, ToV> Map<ToK, ToV> convertEntries(@Nonnull Collection<Entry<FromK, FromV>> entries,
	                                                                    @Nonnull Function<? super FromK, ? extends ToK> keyMapper,
	                                                                    @Nonnull Function<? super FromV, ? extends ToV> valueMapper) {
		Map<ToK, ToV> result = new HashMap<>();
		for (Entry<FromK, FromV> entry : entries) {
			try {
				result.put(keyMapper.apply(entry.getKey()), valueMapper.apply(entry.getValue()));
			} catch (Exception ex) {
				//if (logMappingError)
					//logger.error("Unable map '{}'='{}'", entry.getKey(), entry.getValue(), ex);
			}
		}
		return result;
	}

	public static <T> T getMostFrequentValue(@Nonnull Collection<T> values) {
		List<T> list = new ArrayList<>(values);

		T valueMax = null;
		int max = 0;
		for (T value : values) {
			int frequency = Collections.frequency(list, value);
			if (frequency > max) {
				max = frequency;
				valueMax = value;
			}
		}

		return valueMax;
	}

	public static <T> T getMostFrequentValue(@Nonnull Map<?, T> map) {
		return getMostFrequentValue(map.values());
	}

	@SafeVarargs
	public static <T> Set<T> createSetOf(@Nonnull Collection<T>... collections) {
		Set<T> set = new HashSet<>();
		for (Collection<T> collection : collections) {
			set.addAll(collection);
		}
		return set;
	}

	@Nonnull
	public static <T, K> Map<K, T> createMapOf(@Nonnull Collection<? extends T> collection, @Nonnull Function<? super T, ? extends K> extractor) {
		Map<K, T> map = new LinkedHashMap<>();
		for (T value : collection) {
			map.put(extractor.apply(value), value);
		}
		return map;
	}

}
