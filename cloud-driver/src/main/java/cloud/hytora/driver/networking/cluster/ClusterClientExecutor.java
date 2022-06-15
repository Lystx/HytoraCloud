package cloud.hytora.driver.networking.cluster;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.NetworkExecutor;


import io.netty.channel.Channel;

public interface ClusterClientExecutor extends NetworkExecutor {

    Channel getChannel();

    Document getData();

    String getName();

    boolean isAuthenticated();

    void setAuthenticated(boolean state);

    void setName(String name);

    default Task<Boolean> close() {
        Task<Boolean> task = Task.empty();
        getChannel().close().addListener(future -> {
            if (future.isSuccess()) {
                task.setResult(true);
            } else {
                task.setFailure(future.cause());
            }
        });
        return task;
    }
}
