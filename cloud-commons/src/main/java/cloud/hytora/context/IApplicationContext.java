package cloud.hytora.context;

import cloud.hytora.context.factory.InjectFactory;

public interface IApplicationContext extends InjectFactory {
    void refresh();
}
