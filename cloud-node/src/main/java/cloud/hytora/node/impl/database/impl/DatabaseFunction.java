package cloud.hytora.node.impl.database.impl;

import java.sql.SQLException;

@FunctionalInterface
public interface DatabaseFunction<I, O> {

    O apply(I i) throws SQLException;
}

