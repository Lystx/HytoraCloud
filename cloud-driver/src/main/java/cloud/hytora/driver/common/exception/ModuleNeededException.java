package cloud.hytora.driver.common.exception;

public class ModuleNeededException extends HytoraCloudException {

    public ModuleNeededException(String moduleName) {
        super("For this action the following module is required : '" + moduleName + "'!");
    }
}
