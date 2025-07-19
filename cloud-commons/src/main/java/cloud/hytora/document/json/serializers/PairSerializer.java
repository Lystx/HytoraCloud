package cloud.hytora.document.json.serializers;

import cloud.hytora.common.collection.pair.*;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;
import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;

import java.lang.reflect.Field;


public class PairSerializer extends JsonSerializer<Pair> {

	@Override
	public Pair deserialize(JsonEntity element, Field field, Json json, Class<?>... arguments) {
		if (!element.isArray()) {
			return null;
		}
		JsonArray array = element.asJsonArray();
		int size = array.size();
		switch (size) {
			case 1: return Wrap.of(array.get(0));
			case 2: return Tuple.of(array.get(0), array.get(1));
			case 3: return Triple.of(array.get(0), array.get(1), array.get(2));
			case 4: return Quadro.of(array.get(0), array.get(1), array.get(2), array.get(3));
			default:throw new IllegalStateException("No Pair known for amount of " + size);
		}
	}

	@Override
	public JsonEntity serialize(Pair obj, Json json, Field field) {
		Object[] values = obj.values();
		JsonArray array = new JsonArray(values.length);
		for (Object value : values) {
			array.add(json.toJson(value));
		}
		return array;
	}
}
