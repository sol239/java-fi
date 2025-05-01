package com.github.sol239.javafi.utils.files;

import com.github.sol239.javafi.utils.DataObject;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class to handle the app's postgres database configuration file.
 */
public class ConfigHandler {

    /**
     * The name of the configuration file and also its path.
     */
    public static final String CONFIG_FILE = "config";

    /**
     * The URL of the database.
     */
    public String url = "";

    /**
     * The password of the database.
     */
    public String password = "";

    /**
     * The username of the user.
     */
    public String username = "";

    /**
     * Key is the name of the parameter, value is the value of the parameter.
     */
    public HashMap<String, String> configMap;

    public ConfigHandler() {
        this.configMap = new LinkedHashMap<>();

    }

    /**
     * Creates the configuration file.
     * @return a DataObject with the result of the operation
     */
    public DataObject createConfigFile() {

        File configFile = new File(Path.of(CONFIG_FILE).toString());

        if (configFile.exists()) {
            return new DataObject(200, "server", "Configuration file already exists");
        } else {
            try {
                if (configFile.createNewFile()) {
                    createConfigFileTemplate(CONFIG_FILE);
                    return new DataObject(200, "server", "Configuration file created successfully");
                }
                return new DataObject(400, "server", "Error creating configuration file");
            } catch (IOException e) {
                return new DataObject(400, "server", "Error creating configuration file");
            }
        }
    }

    /**
     * Creates a template for the configuration file.
     * @param path the path to the configuration file
     */
    public void createConfigFileTemplate(String path) {
        List<String> params = getConfigParameters();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String param : params) {
                if (param.equals("configMap")) {
                    continue;
                }
                writer.write(param + " = ");
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the configuration parameters.
     * @return a list of configuration parameters
     */
    public List<String> getConfigParameters() {

        List<String> configParameters = new ArrayList<>();

        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getFields();

        for (Field field : fields) {
            configParameters.add(field.getName());
        }
        configParameters.removeFirst();
        return configParameters;
    }

    /**
     * Fills the configMap with the configuration parameters.
     */
    public void fillConfigMap() {
        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getFields();

        for (int i = 1; i < fields.length - 1; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                configMap.put(field.getName(), (String) field.get(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes the configuration map to the configuration file.
     */
    public void writeConfigMap(String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String key : configMap.keySet()) {
                writer.write(key + " = " + configMap.get(key));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the configuration map from the configuration file.
     */
    public void loadConfigMap(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                configMap.put(parts[0].trim(), parts[1].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints the configuration map.
     */
    public void printConfigMap() {
        for (String key : configMap.keySet()) {
            System.out.println(key + " = " + configMap.get(key));
        }
    }

    /**
     * Returns the configuration map as a string.
     * @return the configuration map as a string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String key : configMap.keySet()) {
            if (key.equals("configMap")) {
                continue;
            }
            sb.append(key).append(" = ").append(configMap.get(key)).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
