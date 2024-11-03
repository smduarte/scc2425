package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

final public class JSON {
    final static ObjectMapper mapper = new ObjectMapper();
    private static final Logger Log = Logger.getLogger(JSON.class.getName());

    synchronized public static String encode(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            Log.severe("Failed to encode object to JSON: " + e.getMessage());
            return "";
        }
    }

    synchronized public static <T> T decode(String json, Class<T> classOf) {
        try {
            return mapper.readValue(json, classOf);
        } catch (JsonProcessingException e) {
            Log.severe("Failed to decode JSON to object: " + e.getMessage());
            return null;
        }
    }

    synchronized public static <T> T decode(String json, TypeReference<T> typeOf) {
        try {
            return mapper.readValue(json, typeOf);
        } catch (JsonProcessingException e) {
            Log.severe("Failed to decode JSON to object: " + e.getMessage());
            return null;
        }
    }
}