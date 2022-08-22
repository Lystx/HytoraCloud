package cloud.hytora.context;

import cloud.hytora.context.factory.InjectFactory;


// TODO: 21.08.2022 documentation
public interface IApplicationContext extends InjectFactory {

    void refresh();
}
