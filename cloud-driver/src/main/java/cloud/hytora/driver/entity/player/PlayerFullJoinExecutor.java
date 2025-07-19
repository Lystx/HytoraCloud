package cloud.hytora.driver.entity.player;

import cloud.hytora.common.function.ObjectComparator;
import cloud.hytora.common.task.Task;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface PlayerFullJoinExecutor {

    Result execute(CloudPlayer cloudPlayer, boolean sentToHub , boolean disconnect);

    interface Checker extends ObjectComparator<CloudPlayer> {

    }

    @AllArgsConstructor
    @Getter
    public static class Result {


        private final ResultType type;
        private final int kickedPlayers;

    }

    static enum ResultType {

        NOT_PERFORMED,

        ALLOWED,

        NO_LOWER_PLAYERS_THAN_SELF,

    }
}
