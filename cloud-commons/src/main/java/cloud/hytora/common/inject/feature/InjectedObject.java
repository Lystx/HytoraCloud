package cloud.hytora.common.inject.feature;

import cloud.hytora.common.inject.base.SimpleInjector;
/**
 * Just a class to automatically fill all fields
 * that are marked with {@link Inject} annotation
 * It just refers to {@link SimpleInjector#injectFields(Object, Class)}
 * but works with default constructor
 */
public abstract class InjectedObject {

    protected InjectedObject() {
        SimpleInjector.instance.injectFields(this, getClass());
    }
}
