package cloud.hytora.common.misc;

import cloud.hytora.common.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;


public final class BukkitReflectionSerializationUtils {

	private BukkitReflectionSerializationUtils() {}

	private static final Logger logger = Logger.newInstance();

	public static boolean isSerializable(@Nonnull Class<?> clazz) {
		try {
			clazz.getMethod("serialize");
			return true;
		} catch (Throwable ex) {
			return false;
		}
	}

	@Nullable
	public static Map<String, Object> serializeObject(@Nonnull Object object) {
		Class<?> classOfObject = object.getClass();
		try {

			Method method = classOfObject.getMethod("serialize");
			method.setAccessible(true);
			Object serialized = method.invoke(object);

			if (!(serialized instanceof Map)) throw new IllegalArgumentException(method + " does not return a Map");

			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) serialized;
			return map;

		} catch (Throwable ex) {
			logger.error("Could not serialize object of type {}", classOfObject, ex);
			return null;
		}
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T deserializeObject(@Nonnull Map<String, Object> map, @Nullable Class<T> classOfT) {
		try {

			Class<?> configurationSerializationClass = Class.forName("org.bukkit.configuration.serialization.ConfigurationSerialization");
			Method method = configurationSerializationClass.getMethod("deserializeObject", Map.class);
			method.setAccessible(true);
			Object object = method.invoke(null, map);

			return (T) object;

		} catch (Throwable ex) {
		}

		if (classOfT == null)
			return null;

		try {

			Method method = classOfT.getMethod("deserialize", Map.class);
			method.setAccessible(true);
			Object object = method.invoke(null, map);

			if (!classOfT.isInstance(object)) throw new IllegalStateException("Deserialization of " + classOfT.getName() + " failed: returned " + (object == null ? null : object.getClass().getName()));
			return classOfT.cast(object);

		} catch (Throwable ex) {
			logger.error("Could not deserialize object of type {}", classOfT, ex);
			return null;
		}
	}

	@Nonnull
	public static String getSerializationName(@Nonnull Class<?> clazz) {
		for (Annotation annotation : clazz.getAnnotations()) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			Object value = ReflectionUtils.getAnnotationValue(annotation);
			switch (annotationType.getSimpleName()) {
				case "DelegateDeserialization":
				case "DelegateSerialization":
				case "SerializableAs":
					if (value instanceof Class) {
						return getSerializationName((Class<?>) value);
					} else if (value instanceof String) {
						return (String) value;
					}
					break;
			}
		}

		return clazz.getName();
	}

}
