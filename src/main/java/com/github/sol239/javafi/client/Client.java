package com.github.sol239.javafi.client;

import com.github.sol239.javafi.DataObject;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    /*
    public static final HashMap<Integer, String> mainMenu = new HashMap<>() {
        {
            put(1, "ADD CSV");
            put(2, "ANALYSE CSV");
            put(3, "SETTINGS");
            put(4, "CONSOLE");
            put(5, "EXIT");
        }
    };

    public static final HashMap<Integer, String> addCsvMenu = new HashMap<>() {
        {
            put(0, "MAIN MENU");
            put(1, "ADD CSV");
            put(2, "PRINT VALID CSV PATHS");
            put(3, "REMOVE CSV");
        }
    };

    public static final HashMap<Integer, String> analyseCsv = new HashMap<>() {
        {
            put(0, "MAIN MENU");
            put(1, "ANALYSE CSV");
        }
    };

    public static final HashMap<Integer, String> settings = new HashMap<>() {
        {
            put(0, "MAIN MENU");
            put(1, "VIEW DATASETS");
        }
    };

    */


    public static void main(String[] args) {

        // server settings
        String hostname = "localhost";
        int port = 12345;
        String clientId = "client-" + System.currentTimeMillis();

        int DELAY = 5000;
        int MAX_TRIES = 5;

        int tries = 0;

        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                connectToServer(hostname, port, clientId, scanner);
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Reconnecting in 5 seconds...");
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


            // DEPRECATED
            /*
            int currentMenu = 0;
            String input;

            while (true) {

                if (currentMenu == 0) {
                    System.out.println("MAIN MENU");
                    CmdController.printCommands(mainMenu);

                    System.out.print("> ");
                    input = scanner.nextLine();

                    if (input.equals("5")) {
                        break;
                    } else {
                        currentMenu = Integer.parseInt(input);
                    }

                } else if (currentMenu == 1) {
                    System.out.println("ADD CSV");
                    CmdController.printCommands(addCsvMenu);

                    System.out.print("> ");
                    input = scanner.nextLine();

                    switch (input) {
                        case "0" -> currentMenu = 0;
                        case "1" -> {
                            System.out.println("Enter the path to the CSV file: ");
                            String path = scanner.nextLine();
                            System.out.println("Path: " + path);

                            System.out.println("Set the name of the table: ");
                            String tableName = scanner.nextLine();
                            System.out.println("Table Name: " + tableName);

                            //TODO: send path to server and make server insert it into the db
                            DataObject dataObject = new DataObject(1, clientId, tableName + " X " + path);
                            objectOutputStream.writeObject(dataObject);


                            // Receive the response from the server
                            Object response = objectInputStream.readObject();
                            if (response instanceof String) {
                                System.out.println(response);
                            }
                        }
                        case "2" -> CsvHandler.printValidCsvPaths();
                        case "3" -> {
                            HashMap<Integer, String> csvPaths = CsvHandler.getCsvPaths();

                            if (csvPaths.size() == 0) {
                                System.out.println("No CSV files found.");
                            } else {

                                for (int i = 0; i < csvPaths.size(); i++) {
                                    System.out.println(i + ": " + csvPaths.get(i));
                                }
                                System.out.println("Enter the number of the CSV file to remove: ");
                                int number = Integer.parseInt(scanner.nextLine());
                                CsvHandler.removeCsvPath(csvPaths.get(number));

                            }
                        }
                    }

                } else if (currentMenu == 2) {
                    System.out.println("ANALYSE CSV");
                    CmdController.printCommands(analyseCsv);

                    System.out.print("> ");
                    input = scanner.nextLine();

                    switch (input) {
                        case "0" -> currentMenu = 0;
                    }

                } else if (currentMenu == 3) {
                    System.out.println("SETTINGS");
                    CmdController.printCommands(settings);

                    System.out.print("> ");
                    input = scanner.nextLine();

                    switch (input) {
                        case "0" -> currentMenu = 0;
                    }
                } else if (currentMenu == 4) {


                    currentMenu = 0;
                } else {
                    System.out.print("> ");
                    input = scanner.nextLine();

                    switch (input) {
                        case "0" -> currentMenu = 0;
                    }
                }


            }*/
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } finally {
            System.out.println("Connection closed.");
        }
    }
}