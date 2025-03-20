package com.github.sol239.javafi;

import java.io.Serial;
import java.io.Serializable;

/**
 * A class representing a data object which is used to communicate between the client and the server.
 */
public class DataObject implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // Recommended for Serializable classes

    /**
     * Client to server: the number of the command to be executed.
     * Server to client: the number representing success or failure.
     */
    private final int number;

    /**
     * Client to server: the client ID.
     * Server to client: the server ID.
     */
    private final String clientId;

    /**
     * Client to server: the command to be executed.
     * Server to client: the result of the command.
     */
    private final String cmd;

    /**
     * Constructor for the DataObject class.
     * @param number the number field.
     * @param clientId the clientId field.
     * @param cmd the cmd field.
     */
    public DataObject(int number, String clientId, String cmd) {
        this.number = number;
        this.clientId = clientId;
        this.cmd = cmd;
    }

    /**
     * Getter for the number field.
     * @return the number field.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Getter for the clientId field.
     * @return the clientId field.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Getter for the cmd field.
     * @return the cmd field.
     */
    public String getCmd() {
        return cmd;
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return String.format("[%d]\n%s",number, cmd);
        // return String.format("%s", cmd);

    }
}

