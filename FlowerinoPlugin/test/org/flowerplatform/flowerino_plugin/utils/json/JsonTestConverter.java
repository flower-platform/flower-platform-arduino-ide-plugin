package org.flowerplatform.flowerino_plugin.utils.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTestConverter {
	public static <T>T fromJsonString(String jsonString, Class<T> expectedClass) throws JsonMappingException, JsonParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonString, expectedClass);
	}
}
