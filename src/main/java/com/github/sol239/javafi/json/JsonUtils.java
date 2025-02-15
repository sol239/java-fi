package com.github.sol239.javafi.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Serialize an object into JSON string.
     *
     * @param object The object to serialize.
     * @param saveToFile Flag indicating whether to save to file.
     * @param filePath Optional file path where the serialized JSON will be saved.
     * @return JSON string representation of the object.
     * @throws IOException if an error occurs during serialization or file writing.
     */
    public static String serialize(Object object, boolean saveToFile, String filePath) throws IOException {
        // Convert the object to JSON string
        String jsonString = objectMapper.writeValueAsString(object);

        if (saveToFile && filePath != null) {
            // If saving to a file, write the JSON string to the specified file
            objectMapper.writeValue(new File(filePath), object);
        }

        return jsonString;
    }

    /**
     * Deserialize JSON file into an object of the specified type.
     *
     * @param classType The class type to deserialize to.
     * @param filePath The path to the JSON file.
     * @param <T> The type of the object.
     * @return The deserialized object.
     * @throws IOException if an error occurs during deserialization.
     */
    public static <T> T deserialize(Class<T> classType, String filePath) throws IOException {
        try {
            return objectMapper.readValue(new File(filePath), classType);
        }

        catch (Exception e) {
            try {
                return classType.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Cannot create instance of " + classType.getName(), ex);
            }
        }
    }
}
