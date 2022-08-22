package cloud.hytora.driver.commands.parameter.defaults;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.parameter.CommandParameterType;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.ICloudPlayerManager;

public class PlayerParamType extends CommandParameterType<ICloudPlayer> {

    @Override
    public String label() {
        return "player";
    }

    @Override
    public ICloudPlayer resolve(String s) {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getCloudPlayerByNameOrNull(s);
    }

    @Override
    public Class<ICloudPlayer> typeClass() {
        return ICloudPlayer.class;
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
