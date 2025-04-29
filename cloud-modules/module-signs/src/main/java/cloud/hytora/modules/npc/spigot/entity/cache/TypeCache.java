package cloud.hytora.modules.npc.spigot.entity.cache;

import cloud.hytora.driver.CloudDriver;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface TypeCache {
    class ClassCache {
        protected static final ConcurrentMap<CacheKey, Object> CACHE = new ConcurrentHashMap<>();

        public static Object find(String name, Class<?> objectClass) {
            return CACHE.get(new CacheKey(name, objectClass));
        }

        public static void register(String name, Object object, Class<?> objectClass) {
            CACHE.putIfAbsent(new CacheKey(name, objectClass), object);
        }

        private static class CacheKey {
            private final Class<?> type;

            private final String value;

            public CacheKey(String value, Class<?> type) {
                this.type = type;
                this.value = value;
            }

            public boolean equals(Object o) {
                if (this == o)
                    return true;
                if (o == null || getClass() != o.getClass())
                    return false;
                CacheKey classKey = (CacheKey) o;
                return (Objects.equals(this.type, classKey.type) && Objects.equals(this.value, classKey.value));
            }

            public int hashCode() {
                return Objects.hash(this.type, this.value);
            }
        }
    }

    class CacheBuilder {
        private static final String EMPTY_STRING = "";

        private final CachePackage cachePackage;

        private final CacheCategory cacheCategory;

        private final String fieldName;

        private final List<String> className;

        private final List<String> methods;

        private final String additionalData;

        private final Class<?> clazz;

        private final ImmutableList<Class<?>[]> parameterTypes;

        private final Class<?> expectType;

        public CacheBuilder(CachePackage cachePackage) {
            this(cachePackage, CacheCategory.DEFAULT, new ArrayList<>(), "", new ArrayList<>(), "",

                    ImmutableList.of(), null);
        }

        protected CacheBuilder(
                CachePackage cachePackage, CacheCategory cacheCategory, List<String> className,
                String fieldName, List<String> methods, String additionalData,
                ImmutableList<Class<?>[]> parameterTypes, Class<?> expectType) {
            this.cachePackage = cachePackage;
            this.cacheCategory = cacheCategory;
            this.className = className;
            this.methods = methods;
            this.fieldName = fieldName;
            this.additionalData = additionalData;
            this.parameterTypes = parameterTypes;
            this.clazz = null;
            this.expectType = expectType;
        }

        public CacheBuilder withCategory(CacheCategory cacheCategory) {
            return new CacheBuilder(this.cachePackage, cacheCategory, this.className, this.fieldName, this.methods, this.additionalData, this.parameterTypes, this.expectType);
        }

        public CacheBuilder withClassName(String className) {
            return new CacheBuilder(this.cachePackage, this.cacheCategory,
                    ImmutableList.<String>builder().addAll(this.className)
                            .add(formatClass(className)).build(), this.fieldName, this.methods, this.additionalData, this.parameterTypes, this.expectType);
        }

        public CacheBuilder withClassName(Class<?> clazz) {
            return new CacheBuilder(this.cachePackage, this.cacheCategory,

                    ImmutableList.<String>builder().addAll(this.className).add((clazz == null) ? "" : clazz.getName()).build(),
                    this.fieldName, this.methods, this.additionalData, this.parameterTypes, this.expectType);
        }

        public CacheBuilder withMethodName(String methodName) {
            return new CacheBuilder(this.cachePackage, this.cacheCategory, this.className, this.fieldName,

                    ImmutableList.<String>builder().addAll(this.methods).add(methodName).build(), this.additionalData, this.parameterTypes, this.expectType);
        }

        public CacheBuilder withFieldName(String fieldName) {
            return new CacheBuilder(this.cachePackage, this.cacheCategory, this.className, fieldName, this.methods, this.additionalData, this.parameterTypes, this.expectType);
        }

        public CacheBuilder withAdditionalData(String additionalData) {
            return new CacheBuilder(this.cachePackage, this.cacheCategory, this.className, this.fieldName, this.methods, additionalData, this.parameterTypes, this.expectType);
        }

        public CacheBuilder withParameterTypes(Class<?>... types) {
            return new CacheBuilder(this.cachePackage, this.cacheCategory, this.className, this.fieldName, this.methods, this.additionalData,

                    ImmutableList.copyOf(Iterables.concat(this.parameterTypes, ImmutableList.of(types))), this.expectType);
        }

        public CacheBuilder withExpectResult(Class<?> expectType) {
            return new CacheBuilder(this.cachePackage, this.cacheCategory, this.className, this.fieldName, this.methods, this.additionalData, this.parameterTypes, expectType);
        }

        protected String formatClass(String className) {
            switch (this.cachePackage) {
                case MINECRAFT_SERVER:
                case CRAFT_BUKKIT:
                    return String.format(((this.cachePackage == CachePackage.CRAFT_BUKKIT) ?
                            this.cachePackage.getFixedPackageName() : this.cachePackage.getForCategory(this.cacheCategory, this.additionalData)) + ".%s", className);
                case DEFAULT:
                    return className;
            }
            throw new IllegalArgumentException("Unexpected package " + this.cachePackage.name());
        }
    }

    abstract class BaseCache<T> {

        protected final CacheBuilder cacheBuilder;

        protected Class<?> BUILDER_CLASS;

        private T cached;
        private boolean loaded = false;

        protected BaseCache(CacheBuilder cacheBuilder) {
            this.cacheBuilder = cacheBuilder;
            for (String classes : cacheBuilder.className) {
                try {
                    this.BUILDER_CLASS = Class.forName(classes);
                } catch (ClassNotFoundException classNotFoundException) {
                    // Ignored...
                }
            }
        }

        public T load() {
            if (loaded)
                return cached;

            try {
                if (BUILDER_CLASS == null) {
                    throw new IllegalStateException(
                            "can't find class for: " + cacheBuilder.className);
                }
                T eval = cached != null ? cached : (cached = onLoad());
                if (eval == null) {
                    throw new NullPointerException();
                }
            } catch (Throwable throwable) {
                if (throwable instanceof IllegalStateException) {
                    CloudDriver.getInstance().getLogger().debug("No cache found for: " + cacheBuilder.className);
                }
                CloudDriver.getInstance().getLogger().debug("No cache found for: " + cacheBuilder.className + " : " + cacheBuilder.methods.toString());
                // skip class...
                CloudDriver.getInstance().getLogger().debug("Skipping cache for " + cacheBuilder.className);
            }
            loaded = true;
            return cached;
        }


        protected abstract T onLoad() throws Exception;

        public static class ClazzLoader extends BaseCache<Class<?>> {
            public ClazzLoader(CacheBuilder cacheBuilder) {
                super(cacheBuilder);
            }

            protected Class<?> onLoad() {
                return this.BUILDER_CLASS;
            }
        }

        public static class MethodLoader extends BaseCache<Method> {
            public MethodLoader(CacheBuilder builder) {
                super(builder);
            }

            protected Method onLoad() {
                Method methodThis = null;
                List<String> methods = this.cacheBuilder.methods;
                boolean hasExpectedType = (this.cacheBuilder.expectType != null);
                if (methods.isEmpty() && hasExpectedType)
                    for (Method method : this.BUILDER_CLASS.getDeclaredMethods()) {
                        if (method.getReturnType() == this.cacheBuilder.expectType)
                            return method;
                    }
                for (String methodName : this.cacheBuilder.methods) {
                    try {
                        Method maybeGet;
                        if (!Iterables.isEmpty(this.cacheBuilder.parameterTypes)) {
                            maybeGet = this.BUILDER_CLASS.getDeclaredMethod(methodName, Iterables.get(this.cacheBuilder.parameterTypes, 0));
                        } else {
                            maybeGet = this.BUILDER_CLASS.getDeclaredMethod(methodName);
                        }
                        if (this.cacheBuilder.expectType != null && this.cacheBuilder.expectType != maybeGet.getReturnType())
                            continue;
                        maybeGet.setAccessible(true);
                        methodThis = maybeGet;
                    } catch (NoSuchMethodException noSuchMethodException) {
                    }
                }
                return methodThis;
            }
        }

        public static class FieldLoader extends BaseCache<Field> {
            public FieldLoader(CacheBuilder cacheBuilder) {
                super(cacheBuilder);
            }

            protected Field onLoad() throws NoSuchFieldException {
                if (this.cacheBuilder.expectType != null)
                    for (Field field1 : this.BUILDER_CLASS.getDeclaredFields()) {
                        if (field1.getType() == this.cacheBuilder.expectType) {
                            field1.setAccessible(true);
                            return field1;
                        }
                    }
                Field field = this.BUILDER_CLASS.getDeclaredField(this.cacheBuilder.fieldName);
                field.setAccessible(true);
                return field;
            }

            public AsValueField asValueField() {
                return new AsValueField(this);
            }

            private static class AsValueField extends BaseCache<Object> {
                private final FieldLoader fieldLoader;

                public AsValueField(FieldLoader fieldLoader) {
                    super(fieldLoader.cacheBuilder);
                    this.fieldLoader = fieldLoader;
                }

                protected Object onLoad() throws IllegalAccessException, NoSuchFieldException {
                    Field field = this.fieldLoader.onLoad();
                    return field.get(null);
                }
            }
        }

        public static class ConstructorLoader extends BaseCache<Constructor<?>> {
            public ConstructorLoader(CacheBuilder cacheBuilder) {
                super(cacheBuilder);
            }

            protected Constructor<?> onLoad() throws NoSuchMethodException {
                Constructor<?> constructor = null;
                if (Iterables.size(this.cacheBuilder.parameterTypes) > 1) {
                    for (Class<?>[] keyParameters : this.cacheBuilder.parameterTypes) {
                        try {
                            constructor = this.BUILDER_CLASS.getDeclaredConstructor(keyParameters);
                        } catch (NoSuchMethodException noSuchMethodException) {

                        }
                    }
                } else {
                    constructor = (Iterables.size(this.cacheBuilder.parameterTypes) > 0) ? this.BUILDER_CLASS.getDeclaredConstructor(Iterables.get(this.cacheBuilder.parameterTypes, 0)) : this.BUILDER_CLASS.getDeclaredConstructor();
                }
                if (constructor != null)
                    constructor.setAccessible(true);
                return constructor;
            }
        }

        public static class EnumLoader extends BaseCache<Enum<?>[]> {
            public EnumLoader(CacheBuilder cacheBuilder) {
                super(cacheBuilder);
            }

            protected Enum<?>[] onLoad() {
                Enum[] arrayOfEnum = (Enum[]) this.BUILDER_CLASS.getEnumConstants();
                for (Enum<?> enumConstant : arrayOfEnum)
                    ClassCache.register(enumConstant.name(), enumConstant, this.BUILDER_CLASS);
                return (Enum<?>[]) arrayOfEnum;
            }
        }
    }
}
