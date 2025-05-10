package com.github.sol239.javafi.demos;

import com.github.sol239.javafi.utils.files.ConfigHandler;

/**
 * Class for demonstrating the ConfigHandler class.
 * Caution: the execution of this class will rewrite the config file.
 */
@Deprecated
public class ConfigDemo {

    public static void main(String[] args) {


        // creates a new config file
        ConfigHandler cf = new ConfigHandler();
        cf.createConfigFile(ConfigHandler.CONFIG_FILE);

        cf.url = "jdbc:postgresql://localhost:5432/javafi";
        cf.password = "password";
        cf.username = "postgres";

        // fills the config map with the Config Handler class fields
        cf.fillConfigMap();

        // writes the config map to the config file
        cf.writeConfigMap(ConfigHandler.CONFIG_FILE);

        // loads the config map from the config file
        ConfigHandler cfg = new ConfigHandler();
        cfg.loadConfigMap(ConfigHandler.CONFIG_FILE);
        cfg.printConfigMap();
    }
}
