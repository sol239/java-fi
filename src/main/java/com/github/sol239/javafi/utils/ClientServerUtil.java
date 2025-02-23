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
     * @return true if the object was sent successfully, false otherwise
     */
    public static boolean sendObject(ObjectOutputStream objectOutputStream, Object dataObject) {
        try {
            objectOutputStream.writeObject(dataObject);
            //objectOutputStream.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Error sending command to server: " + e.getMessage());
            return false;
        }
    }

    public static Object receiveObject(ObjectInputStream objectInputStream) {
        try {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error receiving response: " + e.getMessage());
            return null;
        }
    }

}
