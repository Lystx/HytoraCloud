package cloud.hytora.common.inject.base;


import cloud.hytora.common.inject.IBinder;

public class SimpleBinder<T> implements IBinder<T> {

    /**
     * The class of the class to bind
     */
    private final Class<T> bindingClass;

    /**
     * The name if annotated with is executed
     */
    private String name;

    public SimpleBinder(Class<T> bindingClass) {
        this.bindingClass = bindingClass;
    }

    @Override
    public IBinder<T> annotatedWith(String name) {
        this.name = name;
        return this;
    }

    @Override
    public void toInstance(T instance) {
        SimpleInjector.instance.getRegisterInjectors().put(this.bindingClass, instance);
        if (name != null) {
            SimpleInjector.instance.getNamedInjectors().put(name, instance);
            SimpleInjector.instance.getNamedClassInjectors().put(name, bindingClass);
        }
    }
}
