package com.github.sol239.javafi.server;

import com.github.sol239.javafi.utils.files.ConfigHandler;
import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.cmd.Shell;
import com.github.sol239.javafi.utils.ClientServerUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Server class.
 */
public class ServerParallel {

    /**
     * Server port.
     */
    public static final int PORT = 12345;

    /**
     * List of clients.
     * Synchronized for improved safety.
     */
    private static final List<Socket> clients = Collections.synchronizedList(new ArrayList<>());

    /**
     * Shell instance to execute commands.
     */
    private static Shell sh = new Shell();

    /**
     * Config instance to handle the configuration file.
     */
    private static ConfigHandler cfg = new ConfigHandler();

    /**
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            // db config file
            cfg.createConfigFile();
            cfg.loadConfigMap();

            System.out.println("Server is listening on port " + PORT);
            serverSocket.setReuseAddress(true);
            ExecutorService executorService = java.util.concurrent.Executors.newCachedThreadPool();

            while (true) {
                Socket socket = serverSocket.accept();
                clients.add(socket);
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());
                executorService.execute(new ClientHandler(socket));
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    /**
     * Operation selector based on the number received from client.
     * Currently only one operation is implemented since all commands_to_load
     * from client are sent as a single string.
     * It can be used in the future to implement more operations.
     *
     * @param id  the number of operation received from the client
     * @param cmd the command received from the client
     * @return the response to be sent back to the client
     */
    private static DataObject operationSelector(int id, String cmd) {

        DataObject response = null;
        // Switch is redundant for now, but it can be used in the future
        // to add more operations.
        switch (id) {
            case 2 -> {
                response = runConsoleOperation(cmd);
            }
        }
        return response;
    }

    /**
     * Method used to run client commands_to_load.
     * @param cmd the command string
     * @return the response to be sent back to the client
     */
    public static DataObject runConsoleOperation(String cmd) {
        String[] cmdArray = cmd.split(" ");

        String cmdName = "";

        List<String> flags = new ArrayList<>();
        List<String> args = new ArrayList<>();

        cmdName = cmdArray[0].trim();

        for (int i = 1; i < cmdArray.length; i++) {
            if (cmdArray[i].startsWith("-")) {
                flags.add(cmdArray[i]);
            } else {
                args.add(cmdArray[i]);
            }
        }

        try {
            DataObject response = sh.runCommand(cmdName, args, flags);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return new DataObject(400, "server", "Error running command: " + cmdName);
        }
    }

    /**
     * ClientHandler class.
     */
    private static class ClientHandler implements Runnable {
        /**
         * Number of tries to receive a non-null response from the client.
         */
        public static final int CLIENT_TIMEOUT_TRIES = 10;
        /**
         * The client socket.
         */
        private final Socket socket;

        /**
         * Constructor.
         *
         * @param socket the client socket
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * Run method of the thread.
         */
        @Override
        public void run() {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
                int tries = 0;
                while (true) {

                    // Read the dataObject from client
                    DataObject dataObject = (DataObject) ClientServerUtil.receiveObject(objectInputStream);
                    DataObject response;
                    if (dataObject != null) {

                        if (dataObject.getCmd().equalsIgnoreCase("exit")) {
                            response = new DataObject(0, "server", "exit");
                            ClientServerUtil.sendObject(objectOutputStream, response);
                            break;
                        }

                        response = operationSelector(dataObject.getNumber(), dataObject.getCmd());
                    } else {
                        response = new DataObject(400, "server", "Invalid command format");

                        // if the client force closes the connection, the server will keep trying to send a response
                        // needs to be closed manually after a certain number of received null objects
                        tries++;
                        if (tries > CLIENT_TIMEOUT_TRIES) {
                            break;
                        }
                    }

                    // Send the response to the client
                    ClientServerUtil.sendObject(objectOutputStream, response);


                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                // Close the socket
                clients.remove(socket);
                try {
                    socket.close();
                    System.out.println("Client [" + socket.getInetAddress().getHostAddress() + "] disconnected.");
                } catch (IOException e) {
                    System.out.println("Error closing the socket: " + e.getMessage());
                }
            }
        }
    }
}


