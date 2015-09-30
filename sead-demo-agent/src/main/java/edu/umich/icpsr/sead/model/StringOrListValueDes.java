package edu.umich.icpsr.sead.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class StringOrListValueDes extends JsonDeserializer<List<String>> {
	@Override
	public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		ObjectCodec oc = p.getCodec();
		JsonNode node = oc.readTree(p);
		ArrayList<String> list = new ArrayList<String>();
		if (ArrayNode.class.isAssignableFrom(node.getClass())) {
			Iterator<JsonNode> elements = ((ArrayNode) node).elements();
			while (elements.hasNext()) {
				JsonNode jsonNode = (JsonNode) elements.next();
				list.add(jsonNode.textValue());
			}
		} else {
			list.add(node.textValue());
		}
		return list;
	}
}