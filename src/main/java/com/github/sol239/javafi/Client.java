package com.github.sol239.javafi;

import com.github.sol239.javafi.csv.CsvHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

public class Client {

    public static final HashMap<Integer, String> mainMenu = new HashMap<>() {
        {
            put(1, "ADD CSV");
            put(2, "ANALYSE CSV");
            put(3, "SETTINGS");
            put(4, "EXIT");
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

    public static void main(String[] args) {

        // server settings
        String hostname = "localhost";
        int port = 12345;
        String clientId = "client-" + System.currentTimeMillis();

        CmdController cc = new CmdController();


        try (
                Socket socket = new Socket(hostname, port);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to the server as " + clientId);

            int currentMenu = 0;
            String input;

            while (true) {

                if (currentMenu == 0) {
                    System.out.println("MAIN MENU");
                    CmdController.printCommands(mainMenu);

                    System.out.print("> ");
                    input = scanner.nextLine();

                    if (input.equals("4")) {
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
                            CsvHandler.readCsv(path);
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

                } else {
                    System.out.print("> ");
                    input = scanner.nextLine();

                    switch (input) {
                        case "0" -> currentMenu = 0;
                    }


                }





                    /*

                        // dataObject will be sent to the server
                        DataObject dataObject = new DataObject(number, clientId);
                        // Send the dataObject to the server
                        objectOutputStream.writeObject(dataObject);

                        // Receive the response from the server
                        Object response = objectInputStream.readObject();
                        if (response instanceof String) {
                            System.out.println(response);
                        }

                        */

            }
        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            // for example, this exception will be thrown if the server is not running
            System.out.println("IOException: " + e.getMessage());
        }
    }
}