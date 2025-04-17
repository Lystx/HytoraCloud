package cloud.hytora.node.impl.database.sql.abstraction.where;

import javax.annotation.Nonnull;

public interface SQLWhere {

	@Nonnull
	Object[] getArgs();

	@Nonnull
	String getAsSQLString();

}
