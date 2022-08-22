package cloud.hytora.driver.commands.parameter.defaults;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.parameter.CommandParameterType;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;

public class ServiceParamType extends CommandParameterType<ICloudServer> {

    @Override
    public String label() {
        return "server";
    }

    @Override
    public ICloudServer resolve(String s) {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getServiceByNameOrNull(s);
    }

    @Override
    public Class<ICloudServer> typeClass() {
        return ICloudServer.class;
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
