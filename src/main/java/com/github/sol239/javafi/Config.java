package com.github.sol239.javafi;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Class to handle the app's configuration.
 */
public class Config {
    public static final String CONFIG_FILE = "config";

    public String configFileName = "";
    public String url = "";
    public String password = "";
    public String username = "";

    public HashMap<String, String> configMap;

    public Config() {
        this.configMap = new LinkedHashMap<>();
        this.configFileName = CONFIG_FILE;

    }

    public DataObject createConfigFile() {

        File configFile = new File(Path.of(CONFIG_FILE).toString());

        if (configFile.exists()) {
            return new DataObject(200, "server", "Configuration file already exists");
        } else {
            try {
                if (configFile.createNewFile()) {
                    createConfigFileTemplate();
                    return new DataObject(200, "server", "Configuration file created successfully");
                }
                return new DataObject(400, "server", "Error creating configuration file");
            } catch (IOException e) {
                return new DataObject(400, "server", "Error creating configuration file");
            }
        }
    }

    public void createConfigFileTemplate() {
        List<String> params = getConfigParameters();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
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

    public void writeConfigMap() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            for (String key : configMap.keySet()) {
                writer.write(key + " = " + configMap.get(key));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfigMap() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                configMap.put(parts[0].trim(), parts[1].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printConfigMap() {
        for (String key : configMap.keySet()) {
            System.out.println(key + " = " + configMap.get(key));
        }
    }

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
