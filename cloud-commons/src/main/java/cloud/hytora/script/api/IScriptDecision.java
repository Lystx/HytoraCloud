package cloud.hytora.script.api;

import cloud.hytora.common.function.BiSupplier;

public interface IScriptDecision {

    BiSupplier<IScript, Boolean> getChecker();

    void executeFalse(IScript script);

    void executeTrue(IScript script);

}
