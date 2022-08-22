package cloud.hytora.common.misc;

import cloud.hytora.common.collection.ArrayWalker;
import cloud.hytora.common.collection.ClassWalker;
import cloud.hytora.common.collection.ExposedSecurityManager;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.util.Validation;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;


public final class ReflectionUtils {

	private ReflectionUtils() {}


	/**
	 * Invokes a method
	 *
	 * @param m        The method
	 * @param instance The instance
	 * @param val      The value
	 * @return The object
	 */
	public static Object invokeMethod(Method m, Object instance, Object... val) {
		try {
			if(val.length == 0) {
				return m.invoke(instance);
			}
			else {
				return m.invoke(instance, val);
			}
		}
		catch(IllegalAccessException | InvocationTargetException e) {
			return null;
		}
	}

	public static Object invokeMethod(String name, Class<?> c, Object instance, Object... val) {
		return invokeMethod(getDeclaredMethod(c, name, getParameterTypes(val)), instance, val);
	}

	public static Method getDeclaredMethod(Class<?> c, String name, Class<?>[] parameterTypes) {
		try {
			return c.getMethod(name, parameterTypes);
		}
		catch(NoSuchMethodException e) {
			return null;
		}
	}

	/**
	 * Get parameterTypes
	 *
	 * @param objects The objects
	 * @return The class array (types)
	 */
	public static Class<?>[] getParameterTypes(Object... objects) {
		List<Class<?>> types = new ArrayList<>();

		for(Object o : objects) {
			types.add(o.getClass());
		}
		return types.toArray(new Class<?>[]{});
	}



	/**
	 * Get method by name (declared)
	 *
	 * @param c    The class
	 * @param name The name
	 * @return The method
	 */
	public static Method getDeclaredMethod(Class<?> c, String name) {
		for(Method m : c.getDeclaredMethods()) {
			if(m.getName().equals(name)) return m;
		}
		return null;
	}
	/**
	 * Gets a field from given name out of given class
	 *
	 * @param name  The name of the field
	 * @param clazz The class
	 * @return The field
	 */
	public static Field getField(String name, Class<?> clazz) {
		for(Field f : clazz.getDeclaredFields()) {
			if(f.getName().equals(name)) return f;
		}
		return null;
	}
	/**
	 * Gets the object behind a field from given instance
	 *
	 * @param instance The instance
	 * @param field    The field
	 * @return The object
	 */
	public static Object getFieldObject(Field field, Object instance) {
		try {
			field.setAccessible(true);
			return field.get(instance);
		}
		catch(IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * Casts a string to maybe a number which is behind the char array?<br>
	 * Options are: Numerics, UUIDs, Lists or nothing
	 *
	 * @param s The string
	 * @return The object
	 */
	public static Object safeCast(String s) {
		return Validation.INTEGER.matches(s) ? Integer.valueOf(s)
				: Validation.LONG.matches(s) ? Long.valueOf(s)
				: Validation.DOUBLE.matches(s) ? Double.valueOf(s)
				: Validation.UNIQUEID.matches(s) ? UUID.fromString(s)
				: Validation.LIST.matches(s) ? safeCast(StringUtils.split(s.replace("[", "").replace("]", ""), ", "))
				: s.equals("null") ? null
				: s;
	}

	public static List<Object> safeCast(List<String> s) {
		List<Object> l = new ArrayList<>();
		if(!s.isEmpty() && !s.get(0).isEmpty()) s.forEach(s1 -> l.add(safeCast(s1)));
		return l;
	}

	/**
	 * Casts the given string like {@link #safeCast(String)} but it minds the type (as class)<br>
	 * This can be used to cast a string for a specific field
	 *
	 * @param s The string
	 * @param c The class (type)
	 * @return The casted object
	 */
	public static Object safeCast(String s, Class<?> c) {
		if(Validation.NUMBER.matches(s)) {
			if(c.equals(Short.class) || c.equals(short.class)) return Short.valueOf(s);
			else if(c.equals(Integer.class) || c.equals(int.class)) return Integer.valueOf(s);
			else if(c.equals(Long.class) || c.equals(long.class)) return Long.valueOf(s);
		}
		else if(s.equalsIgnoreCase("true")
				|| s.equalsIgnoreCase("false")) return Boolean.valueOf(s);
		else if(Enum.class.isAssignableFrom(c)) {
			for(Object e : c.getEnumConstants()) {
				if((e + "").equalsIgnoreCase(s)) {
					return e;
				}
			}
		}
		else {
			return safeCast(s);
		}
		return s;
	}

	public static Object safeCast(String s, Field f) {
		return safeCast(s, f.getType());
	}

	/**
	 * Checks a method for different conditions
	 *
	 * @param m           The method
	 * @param beStatic    Method must be static?
	 * @param bePublic    Method must be public?
	 * @param returnType  Method must return ..?
	 * @param annotations Method must have annotations..?
	 * @param parameter   Method must have parameter..?
	 * @return The result
	 */
	public static boolean checkMethod(Method m, boolean beStatic, boolean bePublic, Class<?> returnType,
									  Class<? extends Annotation>[] annotations, Class<?>[] parameter) {
		// check modifier
		if((beStatic != Modifier.isStatic(m.getModifiers()))
				|| (bePublic != Modifier.isPublic(m.getModifiers()))) {
			return false;
		}

		// check return type
		if((returnType == null && !m.getReturnType().equals(Void.TYPE))
				|| (returnType != null && !returnType.isAssignableFrom(m.getReturnType()))) {
			return false;
		}

		// check annotations
		for(Class<? extends Annotation> an : annotations) {
			if(!m.isAnnotationPresent(an)) {
				return false;
			}
		}

		// check parameter
		for(int i = 0; i < m.getParameters().length; i++) {
			if(i >= parameter.length) return false;
			Class<?> paramType = m.getParameters()[i].getType();
			if(!parameter[i].isAssignableFrom(paramType)) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkMethod(Method m, Class<? extends Annotation> annotation, Class<?>[] parameter) {
		return checkMethod(m, false, true, null, new Class[]{annotation}, parameter);
	}

	/**
	 * Clears the console screen
	 */
	public static void clearConsole() {
		try {
			String os = System.getProperty("os.name");

			if (os.contains("Windows")) {
				new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			} else {
				Runtime.getRuntime().exec("clear");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Nonnull
	public static Collection<Method> getPublicMethodsAnnotatedWith(@Nonnull Class<?> clazz, @Nonnull Class<? extends Annotation> annotationClass) {
		List<Method> annotatedMethods = new ArrayList<>();
		for (Method method : clazz.getMethods()) {
			if (!method.isAnnotationPresent(annotationClass)) continue;
			annotatedMethods.add(method);
		}
		return annotatedMethods;
	}

	@SafeVarargs
	@Nonnull
	public static Collection<Method> getMethodsAnnotatedWith(@Nonnull Class<?> clazz, @Nonnull Class<? extends Annotation>... annotationClass) {
		List<Method> annotatedMethods = new ArrayList<>();
		for (Class<?> currentClass : ClassWalker.walk(clazz)) {
			for (Method method : currentClass.getDeclaredMethods()) {
				for (Class<? extends Annotation> aClass : annotationClass) {
					if (method.getAnnotation(aClass) != null) {
						annotatedMethods.add(method);
					}
				}
			}
		}
		return annotatedMethods;
	}

	@Nonnull
	public static Method getInheritedPrivateMethod(@Nonnull Class<?> clazz, @Nonnull String name, @Nonnull Class<?>... parameterTypes) throws NoSuchMethodException {
		for (Class<?> current : ClassWalker.walk(clazz)) {
			try {
				return current.getDeclaredMethod(name, parameterTypes);
			} catch (Throwable ex) {
			}
		}

		throw new NoSuchMethodException(name);
	}

	@Nonnull
	public static Field getInheritedPrivateField(@Nonnull Class<?> clazz, @Nonnull String name) throws NoSuchFieldException {
		for (Class<?> current : ClassWalker.walk(clazz)) {
			try {
				return current.getDeclaredField(name);
			} catch (Throwable ex) {
			}
		}

		throw new NoSuchFieldException(name);
	}

	/**
	 * @param classOfEnum The class containing the enum constants
	 * @return The first enum found by the given names
	 */
	@Nonnull
	public static <E extends Enum<E>> E getFirstEnumByNames(@Nonnull Class<E> classOfEnum, @Nonnull String... names) {
		for (String name : names) {
			try {
				return Enum.valueOf(classOfEnum, name);
			} catch (IllegalArgumentException | NoSuchFieldError ex) { }
		}
		throw new IllegalArgumentException("No enum found in " + classOfEnum.getName() + " for " + Arrays.toString(names));
	}



	/**
	 * Creates an Object from scratch
	 *
	 * @param tClass the object class
	 */
	public static <T> T createEmpty(Class<T> tClass) {

		try {
			return tClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			try {
				Constructor<?> constructor;

				try {
					List<Constructor<?>> constructors = Arrays.asList(tClass.getDeclaredConstructors());

					constructors.sort(Comparator.comparingInt(Constructor::getParameterCount));

					constructor = constructors.get(constructors.size() - 1);
				} catch (Exception ex) {
					constructor = null;
				}

				//Iterates through all Constructors to create a new Instance of the Object
				//And to set all values to null, -1 or false
				T object = null;
				if (constructor != null) {
					Object[] args = new Object[constructor.getParameters().length];
					for (int i = 0; i < constructor.getParameterTypes().length; i++) {
						Class<?> parameterType = constructor.getParameterTypes()[i];
						if (Number.class.isAssignableFrom(parameterType)) {
							args[i] = -1;
						} else if (parameterType.equals(boolean.class) || parameterType.equals(Boolean.class)) {
							args[i] = false;
						} else if (parameterType.equals(int.class) || parameterType.equals(double.class) || parameterType.equals(short.class) || parameterType.equals(long.class) || parameterType.equals(float.class) || parameterType.equals(byte.class)) {
							args[i] = -1;
						} else if (parameterType.equals(Integer.class) || parameterType.equals(Double.class) || parameterType.equals(Short.class) || parameterType.equals(Long.class) || parameterType.equals(Float.class) || parameterType.equals(Byte.class)) {
							args[i] = -1;
						} else {
							args[i] = null;
						}
					}
					object = (T) constructor.newInstance(args);
				}

				if (object == null) {
					object = tClass.newInstance();
				}

				return object;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Iterates through an array which may contain primitive data types or non primitive data types and performs the given action on each element.
	 * Because we can't just cast such an array to {@code Object[]}, we have to use some reflections.
	 *
	 * @param array The target array, as {@link Object}; Can't use an array type here.
	 * @param <T> The type of data we will cast the content to. Use {@link Object} if the it's unknown.
	 *
	 * @throws IllegalArgumentException
	 *         If the {@code array} is not an actual array
	 *
	 * @see Array
	 * @see Array#getLength(Object)
	 * @see Array#get(Object, int)
	 */
	public static <T> void forEachInArray(@Nonnull Object array, @Nonnull Consumer<T> action) {
		ReflectionUtils.<T>newArrayIterable(array).forEach(action);
	}

	@Nonnull
	@CheckReturnValue
	public static <T> Iterable<T> newArrayIterable(@Nonnull Object array) {
		return ArrayWalker.walkObject(array);
	}

	@CheckReturnValue
	public static Class<?> getCaller(int index) {
		try {
			return new ExposedSecurityManager().getPublicClassContext()[index + 2];
		} catch (Exception ex) {
			throw new WrappedException(ex);
		}
	}

	@CheckReturnValue
	public static Class<?> getCaller() {
		return getCaller(2);
	}

	@Nonnull
	public static String getCallerName() {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		StackTraceElement element = trace[3];

		String className = StringUtils.getAfterLastIndex(element.getClassName(), ".");
		return className + "." + element.getMethodName();
	}

	/**
	 * Takes an {@link Enum} and returns the corresponding {@link Field} using {@link Class#getField(String)}
	 *
	 * @see Class#getField(String)
	 */
	@Nonnull
	public static Field getEnumAsField(@Nonnull Enum<?> enun) {
		Class<?> enumClass = enun.getClass();

		try {
			return enumClass.getField(enun.name());
		} catch (NoSuchFieldException ex) {
			throw new WrappedException(ex);
		}
	}

	/**
	 * @see Field#getAnnotations()
	 */
	@Nonnull
	public static <E extends Enum<?>> Annotation[] getEnumAnnotations(@Nonnull E enun) {
		Field field = getEnumAsField(enun);
		return field.getAnnotations();
	}

	/**
	 * @return Returns {@code null} if no annotation of this class is present
	 *
	 * @see Field#getAnnotation(Class)
	 */
	public static <E extends Enum<?>, A extends Annotation> A getEnumAnnotation(@Nonnull E enun, Class<A> classOfAnnotation) {
		Field field = getEnumAsField(enun);
		return field.getAnnotation(classOfAnnotation);
	}

	@Nullable
	public static <E extends Enum<E>> E getEnumOrNull(@Nullable String name, @Nonnull Class<E> classOfEnum) {
		try {
			if (name == null) return null;
			return Enum.valueOf(classOfEnum, name);
		} catch (Exception ex) {
			return null;
		}
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClassOrNull(@Nullable String name) {
		try {
			if (name == null) return null;
			return (Class<T>) Class.forName(name);
		} catch (Exception ex) {
			return null;
		}
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClassOrNull(@Nullable String name, boolean initialize, @Nonnull ClassLoader classLoader) {
		try {
			if (name == null) return null;
			return (Class<T>) Class.forName(name, initialize, classLoader);
		} catch (Exception ex) {
			return null;
		}
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <T> T invokeMethodOrNull(@Nullable Object instance, @Nonnull Method method) {
		try {
			if (!method.isAccessible()) method.setAccessible(true);
			return (T) method.invoke(instance);
		} catch (Throwable ex) {
			return null;
		}
	}

	@Nullable
	public static <T> T invokeStaticMethodOrNull(@Nonnull Class<?> clazz, @Nonnull String method) {
		try {
			return invokeMethodOrNull(null, clazz.getMethod(method));
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}

	@Nullable
	public static <T> T invokeMethodOrNull(@Nonnull Object instance, @Nonnull String method) {
		try {
			return invokeMethodOrNull(instance, instance.getClass().getDeclaredMethod(method));
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}

	@Nullable
	public static <T> T getAnnotationValue(@Nonnull Annotation annotation) {
		return invokeMethodOrNull(annotation, "value");
	}

	@Nullable
	public static <E extends Enum<?>> E getEnumByAlternateNames(@Nonnull Class<E> classOfE, @Nonnull String input) {
		E[] values = invokeStaticMethodOrNull(classOfE, "values");
		String[] methodNames = { "getName", "getNames", "getAlias", "getAliases", "getKey", "getKeys", "name", "toString", "ordinal", "getId", "id" };
		for (E value : values) {
			for (String method : methodNames) {
				if (check(input, invokeMethodOrNull(value, method)))
					return value;
			}
		}

		return null;
	}

	private static boolean check(@Nonnull String input, @Nullable Object value) {
		if (value == null) return false;
		if (value.getClass().isArray()) {
			for (Object key : newArrayIterable(value)) {
				if (input.equalsIgnoreCase(String.valueOf(key)))
					return true;
			}
		}
		return input.equalsIgnoreCase(String.valueOf(value));
	}

}
