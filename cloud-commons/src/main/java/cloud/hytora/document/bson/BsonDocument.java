package cloud.hytora.document.bson;

import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentWrapper;
import cloud.hytora.document.IEntry;
import cloud.hytora.document.abstraction.AbstractDocument;
import org.bson.BsonArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;


public class BsonDocument extends AbstractDocument implements DocumentWrapper<org.bson.Document> {

	protected org.bson.Document bsonDocument;

	public BsonDocument() {
		this(new org.bson.Document(), new AtomicBoolean(true));
	}

	public BsonDocument(String json) {
		this(org.bson.Document.parse(json), new AtomicBoolean(true));
	}

	public BsonDocument(org.bson.Document bsonDocument) {
		this(bsonDocument, new AtomicBoolean(true));
	}


	public BsonDocument(@Nonnull org.bson.Document bsonDocument, @Nonnull AtomicBoolean editable) {
		super(editable);
		this.bsonDocument = bsonDocument;
	}

	public BsonDocument(@Nonnull File file) throws IOException {
		this(FileUtils.newBufferedReader(file));
	}

	public BsonDocument(@Nonnull Reader reader) {
		super(true);
		BufferedReader buffered = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
		StringBuilder content = new StringBuilder();
		buffered.lines().forEach(content::append);
		bsonDocument = org.bson.Document.parse(content.toString());
	}

	@Nonnull
	@Override
	public Map<String, Object> toMap() {
		return CollectionUtils.convertMap(bsonDocument, Function.identity(), value -> new BsonEntry(value).toObject());
	}

	@Nonnull
	@Override
	public Map<String, IEntry> toEntryMap() {
		return CollectionUtils.convertMap(bsonDocument, Function.identity(), BsonEntry::new);
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return BsonHelper.toJson(bsonDocument, false);
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return BsonHelper.toJson(bsonDocument, true);
	}

	@Override
	public int size() {
		return bsonDocument.size();
	}

	@Override
	public boolean contains(@Nonnull String path) {
		return bsonDocument.containsKey(path);
	}

	@Override
	public boolean has(String path) {
		return bsonDocument.containsKey(path);
	}

	@Nonnull
	@Override
	public Collection<String> keys() {
		return bsonDocument.keySet();
	}

	@Nonnull
	@Override
	public IEntry get(@Nonnull String path) {
		Object value = bsonDocument.get(path);
		return new BsonEntry(value);
	}

	@Nonnull
	@Override
	public Bundle getBundle(@Nonnull String path) {
		BsonArray array = bsonDocument.get(path, BsonArray.class);
		if (array == null)
			bsonDocument.get(path, array = new BsonArray());
		return new BsonBundle(array, editable);
	}

	@Nonnull
	@Override
	public Document getDocument(@Nonnull String path) {
		org.bson.Document document = bsonDocument.get(path, org.bson.Document.class);
		if (document == null)
			bsonDocument.put(path, document = new org.bson.Document());
		return new BsonDocument(document, editable);
	}

	@Override
	public void write(@Nonnull Writer writer) {
		BsonHelper.toJson(bsonDocument, true, writer);
	}

	@Override
	protected void set0(@Nonnull String path, @Nullable Object value) {
		bsonDocument.put(path, value);
	}

	@Override
	protected void remove0(@Nonnull String path) {
		bsonDocument.remove(path);
	}

	@Override
	protected void clear0() {
		bsonDocument.clear();
	}

	@Override
	public String toString() {
		return this.asRawJsonString();
	}

	@Override
	public org.bson.Document getWrapper() {
		return this.bsonDocument;
	}
}
