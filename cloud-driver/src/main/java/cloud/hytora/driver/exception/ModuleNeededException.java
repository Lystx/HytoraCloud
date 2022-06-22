package cloud.hytora.driver.exception;

public class ModuleNeededException extends CloudException {

    public ModuleNeededException(String moduleName) {
        super("For this action the following module is required : '" + moduleName + "'!");
    }
}
