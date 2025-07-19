package cloud.hytora.driver.networking.query;

import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public interface QueryResponse extends QueryObject<QueryResponse> {

    @Nonnull
    QueryState getState();

    QueryResponse setState(QueryState state);

    @Nullable
    Throwable getError();

    QueryResponse setError(Throwable error);

    void execute();
}
