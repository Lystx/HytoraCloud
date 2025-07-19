package cloud.hytora.driver.networking.query;

import cloud.hytora.common.task.Task;

public interface QueryRequest extends QueryObject<QueryRequest> {

    QueryResponse respond();

    Task<QueryResponse> executeDelayed(int ticks);

    Task<QueryResponse> execute();

    QueryResponse syncUninterruptedlyAndExecute();

}
