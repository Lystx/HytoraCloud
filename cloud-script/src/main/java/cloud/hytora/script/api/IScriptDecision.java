package cloud.hytora.script.api;

import cloud.hytora.common.function.BiSupplier;

import java.util.function.Supplier;

public interface IScriptDecision {

    BiSupplier<IScript, Boolean> getChecker();

    void executeFalse(IScript script);

    void executeTrue(IScript script);

}
