package com.github.sol239.javafi;

import java.io.Serializable;
import java.util.HashMap;

public class DataObject implements Serializable {
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    private final int number;
    private final String clientId;
    private final HashMap<String, String> commands = new HashMap<>();


    public DataObject(int number, String clientId) {
        this.number = number;
        this.clientId = clientId;
    }

    public int getNumber() {
        return number;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public String toString() {
        return "Client ID: " + clientId + ", Number: " + number;
    }
}

