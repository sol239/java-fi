package com.github.sol239.javafi;

import java.io.Serializable;
import java.util.HashMap;

public class DataObject implements Serializable {
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private final int number;
    private final String clientId;
    private final String cmd;

    public DataObject(int number, String clientId, String cmd) {
        this.number = number;
        this.clientId = clientId;
        this.cmd = cmd;
    }

    public int getNumber() {
        return number;
    }

    public String getClientId() {
        return clientId;
    }

    public String getCmd() {
        return cmd;
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "number=" + number +
                ", clientId='" + clientId + '\'' +
                ", cmd='" + cmd + '\'' +
                '}';
    }
}

