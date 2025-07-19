package cloud.hytora.remote.adapter;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.ServiceCycleData;

import java.util.function.Predicate;

public interface RemoteAdapter {


    void setLoginChecker(BiSupplier<CloudPlayer, LoginCheckResult> checker);

    BiSupplier<CloudPlayer, LoginCheckResult> getLoginChecker();

    void executeCommand(String command);

    ServiceCycleData createCycleData();

    Task<Boolean> shutdown();
}
