package com.github.sol239.javafi.client;

import com.github.sol239.javafi.DataObject;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {

        // server settings
        String hostname = "localhost";
        int port = 12345;
        String clientId = "client-" + System.currentTimeMillis();

        int DELAY = 2000;
        int MAX_TRIES = 5;

        int tries = 0;

        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                connectToServer(hostname, port, clientId, scanner);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Reconnecting in 2 seconds...");
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
            tries++;
            if (tries >= MAX_TRIES) {
                System.out.println("Max tries reached. Exiting...");
                break;
            }
        }

    }

    public static void connectToServer(String hostname, int port, String clientId, Scanner scanner) {

        try (
                Socket socket = new Socket(hostname, port);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        ) {
            System.out.println("Connected to the server as " + clientId);


            // console loop
            System.out.println("CONSOLE");

            while (true) {
                System.out.print("> ");

                if (!scanner.hasNextLine()) {
                    System.out.println("No input detected. Exiting...");
                    break;
                }

                String consoleInput = scanner.nextLine().trim();
                if (consoleInput.equalsIgnoreCase("x")) {
                    break;
                }

                DataObject dataObject = new DataObject(2, clientId, consoleInput);

                try {
                    objectOutputStream.writeObject(dataObject);
                    objectOutputStream.flush();
                } catch (IOException e) {
                    System.out.println("Error sending command to server: " + e.getMessage());
                    break;
                }

                try {
                    DataObject response = (DataObject) objectInputStream.readObject();
                    System.out.println(response);
                } catch (EOFException e) {
                    System.out.println("Server disconnected.");
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error receiving response: " + e.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } finally {
            System.out.println("Connection closed.");
        }
    }
}