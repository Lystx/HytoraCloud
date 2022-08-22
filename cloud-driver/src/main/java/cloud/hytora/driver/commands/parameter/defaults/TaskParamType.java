package cloud.hytora.driver.commands.parameter.defaults;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.parameter.CommandParameterType;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
import cloud.hytora.driver.services.task.IServiceTask;

public class TaskParamType extends CommandParameterType<IServiceTask> {

    @Override
    public String label() {
        return "task";
    }

    @Override
    public IServiceTask resolve(String s) {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getTaskByNameOrNull(s);
    }

    @Override
    public Class<IServiceTask> typeClass() {
        return IServiceTask.class;
    }

    @Override
    public boolean checkCustom(String arg, String s) {
        return true;
    }

    @Override
    public String handleCustomException(String s) {
        return null;
    }
}
