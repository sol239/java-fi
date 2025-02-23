package com.github.sol239.javafi.client;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.utils.ClientServerUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client class.
 */
public class Client {

    /**
     * Server port.
     */
    public static final int PORT = 12345;

    /**
     * Reconnect delay in milliseconds.
     */
    public static final int RECONNECT_DELAY = 2000;

    /**
     * Maximum number of reconnect tries before exiting the app.
     */
    public static final int MAX_RECONNECT_TRIES = 5;

    /**
     * Server hostname.
     */
    public static final String hostname = "localhost";

    /**
     * The ID of the client.
     */
    public static final String clientId = "client-" + System.currentTimeMillis();

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        int tries = 0;
        Scanner scanner = new Scanner(System.in);
        boolean reconnect = true;

        while (reconnect) {

            // Connect to the server
            try {
                reconnect = connectToServer(hostname, PORT, clientId, scanner);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Reconnect: " + reconnect);

            // Exit if reconnect is false
            if (!reconnect) {
                break;
            }

            // Reconnect
            System.out.printf("Reconnecting in %d ms...\n", RECONNECT_DELAY);
            try {
                Thread.sleep(RECONNECT_DELAY);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
            tries++;
            if (tries >= MAX_RECONNECT_TRIES) {
                System.out.println("Max tries reached. Exiting...");
                break;
            }
        }
    }

    /**
     * Connect to the server.
     * Scanner cannot be closed here, as it will close System.in. And reconnection will cause an error.
     *
     * @param hostname the server hostname
     * @param port     the server port
     * @param clientId the client ID
     * @param scanner  the scanner
     * @return true if the connection should be reestablished, false otherwise
     */
    public static boolean connectToServer(String hostname, int port, String clientId, Scanner scanner) {
        try (Socket socket = new Socket(hostname, port); ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());) {
            System.out.println("Connected to the server as " + clientId);

            // CONSOLE LOOP
            while (true) {
                System.out.print("> ");

                if (!scanner.hasNextLine()) {
                    System.out.println("No input detected. Exiting...");
                    return true;
                }

                String consoleInput = scanner.nextLine().trim();
                if (consoleInput.equalsIgnoreCase("exit")) {
                    return false;
                }

                DataObject dataObject = new DataObject(2, clientId, consoleInput);
                ClientServerUtil.sendObject(objectOutputStream, dataObject);
                DataObject response = (DataObject) ClientServerUtil.receiveObject(objectInputStream);
                System.out.println(response);

                if (response == null) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
            return true;
        } finally {
            System.out.println("Connection closed.");
        }
    }
}