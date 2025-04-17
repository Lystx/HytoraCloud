package cloud.hytora.node.impl.database.mongo.query;


import cloud.hytora.document.bson.BsonDocument;

import javax.annotation.Nonnull;

public final class MongoDBResult extends BsonDocument {

	public MongoDBResult(@Nonnull org.bson.Document bsonDocument) {
		super(bsonDocument);
	}

	@Override
	public boolean canEdit() {
		return true;
	}

}
