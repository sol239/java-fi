package com.github.sol239.javafi;

import com.github.sol239.javafi.postgre.DBHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerParallel {
    private static int sum = 0; // Shared resource, needs synchronization
    private CmdController cc = new CmdController();


    public static void main(String[] args) {
        int port = 12345;
        DBHandler dbc = new DBHandler();


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected.");

                // Handle each client in a separate thread
                new Thread(new ClientHandler(socket)).start();
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }


    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try ( ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                  ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
                System.out.println("Client connected.");

                while (true) {
                    try {

                        // Reads the object from the client
                        Object input = objectInputStream.readObject();

                        // Check whether the object is an instance of DataObject
                        if (input instanceof DataObject) {

                            // Cast the object to DataObject
                            DataObject data = (DataObject) input;

                            int cmd = data.getNumber();

                            objectOutputStream.writeObject("Sum updated to " + sum + " by " + data.getClientId());
                        } else {
                            objectOutputStream.writeObject("Invalid object. Please send a valid DataObject.");
                        }
                    } catch (ClassNotFoundException e) {
                        objectOutputStream.writeObject("Error: Received unknown object type.");
                    }
                }
            }catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            }
        }
    }
}
