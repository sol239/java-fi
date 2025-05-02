package com.github.sol239.javafi.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility class for client-server communication.
 */
public class ClientServerUtil {

    /**
     * Send an object to the server
     * @param objectOutputStream the object output stream
     * @param dataObject the object to be sent
     */
    public static void sendObject(ObjectOutputStream objectOutputStream, Object dataObject) {
        try {
            objectOutputStream.writeObject(dataObject);
        } catch (IOException e) {
            System.out.println("Error sending command to server: " + e.getMessage());
        }
    }

    /**
     * Receive an object from the server
     * @param objectInputStream the object input stream
     * @return the object received from the server
     */
    public static Object receiveObject(ObjectInputStream objectInputStream) {
        try {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error receiving response: " + e.getMessage());
            return null;
        }
    }

}
