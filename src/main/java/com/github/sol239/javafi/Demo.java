package com.github.sol239.javafi;

import com.github.sol239.javafi.utils.files.ConfigHandler;

@Deprecated
public class Demo {

    public static void main(String[] args) {
        ConfigHandler cf = new ConfigHandler();
        cf.createConfigFile();
        cf.fillConfigMap();
        cf.writeConfigMap();

        ConfigHandler cfg = new ConfigHandler();
        cfg.loadConfigMap();
        cfg.printConfigMap();
    }
}
