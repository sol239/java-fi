package com.github.sol239.javafi;

@Deprecated
public class Demo {

    public static void main(String[] args) {
        Config cf = new Config();
        cf.createConfigFile();
        cf.fillConfigMap();
        cf.writeConfigMap();

        Config cfg = new Config();
        cfg.loadConfigMap();
        cfg.printConfigMap();
    }
}
