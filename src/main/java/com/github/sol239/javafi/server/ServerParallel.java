package com.github.sol239.javafi.server;

import com.github.sol239.javafi.CmdController;
import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.instruments.SqlHandler;
import com.github.sol239.javafi.instruments.SqlInstruments;
import com.github.sol239.javafi.postgre.DBHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class ServerParallel {
    private static int sum = 0; // Shared resource, needs synchronization
    private CmdController cc = new CmdController();


    public static void main(String[] args) {
        int port = 12345;


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

    private static void operationSelector(int id, String cmd) {
        switch (id) {
            case 1 -> insertToDB(cmd);
            case 2 -> runConsoleOperation(cmd);
        }
    }

    private static void insertToDB(String cmd) {

        // cmd:
        // btc X C/Users/user/Downloads/BTC-USD.csv
        String[] cmdArray = cmd.split(" X ");
        DBHandler db = new DBHandler();
        String tableName;
        String csvPath;

        try {
            tableName = cmdArray[0];
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
            return;
        }

        try {
            csvPath = cmdArray[1];
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
            return;
        }

        System.out.println("Operation: insertToDB");
        System.out.println(tableName);
        System.out.println(csvPath);

        try {
            db.insertCsvData(tableName, csvPath);
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
        }
    }

    public static String getIndicatorString(String indicatorString) {
        return indicatorString.split(":")[0];
    }

    public static String[] getIndicatorArguments(String indicatorString) {
        return indicatorString.split(":")[1].split(";");
    }

    /**
     * @param cmd
     */
    public static void runConsoleOperation(String cmd) {
        String[] cmdArray = cmd.split(" X ");


        String operationName;
        String tables;
        String operationString;

        try {
            operationName = cmdArray[0];
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
            return;
        }

        try {
            tables = cmdArray[1];
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
            return;
        }

        try {
            operationString = cmdArray[2];
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
            return;
        }

        System.out.println("Operation: " + operationName);
        System.out.println("Tables: " + tables);
        System.out.println("Operation String: " + operationString);

        switch (operationName) {
            case "st" -> {
                String[] tableArray = tables.split(",");
                String[] instrumentArray = operationString.split(",");

                List<String> symbols = new ArrayList<>();
                List<List<String>> argumentsArray = new ArrayList<>();

                for (String x : instrumentArray) {
                    String symbol = getIndicatorString(x);
                    String[] arguments = getIndicatorArguments(x);

                    symbols.add(symbol);
                    argumentsArray.add(Arrays.asList(arguments));
                }

                HashMap<String, List<String>> paramsMap = new HashMap<>();
                for (int i = 0; i < symbols.size(); i++) {
                    String symbol = symbols.get(i);
                    List<String> arguments = argumentsArray.get(i);
                    paramsMap.put(symbol, arguments);
                }

                SqlInstruments obj = new SqlInstruments();
                Class<?> cls = obj.getClass();

                /*for (int i = 0; i < symbols.size(); i++) {
                    String symbol = symbols.get(i);
                    List<String> arguments = argumentsArray.get(i);

                }*/


                // reflection
                for (String tableName : tableArray) {
                    for (String methodName : symbols) {
                        for (Method method : cls.getDeclaredMethods()) {

                            if (!method.getName().equals(methodName)) {
                                continue;
                            }

                            List<String> _arguments = new ArrayList<>();
                            _arguments.add(tableName);
                            _arguments.addAll(paramsMap.get(methodName));

                            //System.out.println("Table: " + tableName);
                            //System.out.println("Method: " + method.getName());
                            //System.out.println("Arguments:");
                            for (String arg : _arguments) {
                                System.out.println(arg);
                            }

                            Parameter[] parameters = method.getParameters();
                            Object[] arguments = new Object[parameters.length];

                            for (int i = 0; i < parameters.length; i++) {
                                arguments[i] = convertToType(_arguments.get(i), parameters[i].getType());
                                //System.out.println(parameters[i].getType());
                            }

                            System.out.print(method.getName() + "(" );
                            for (int i = 0; i < arguments.length - 1; i++) {
                                System.out.print(arguments[i] + ", ");
                            }
                            System.out.println(arguments[arguments.length - 1] + ")");



                            String result = "";


                            // invoke
                            try {
                               result = (String) method.invoke(obj, arguments);
                            } catch (Exception e) {
                                //System.out.println("FAIL - " + e.getMessage());
                                e.printStackTrace();
                            }
                            System.out.println("Result: ");
                            System.out.println(result);

                            SqlHandler.executeQuery(result);

                            /*
                            System.out.print("Method: " + method.getName());
                            System.out.print(" | Return Type: " + method.getReturnType().getSimpleName());
                            System.out.print(" | Parameters: ");
                            System.out.print(" | Param Types: " + Arrays.toString(arguments));
                            */


                        }
                    }
                }


            }
        }

    }

    public static Object convertToType(String input, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(input);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(input);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(input);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(input);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(input);
        } else if (type == String.class) {
            return input;
        }
        return null;
    }

    public static Map<String, Double[]> getIndicators(String[] indicatorStrings) {
        Map<String, Double[]> indicators = new java.util.HashMap<>();
        System.out.println("getIndicators");
        for (String indicator : indicatorStrings) {
            System.out.println(indicator);
        }
        return indicators;

    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
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
                            System.out.println(data);
                            int id = data.getNumber();
                            String cmd = data.getCmd();
                            operationSelector(id, cmd);


                            objectOutputStream.writeObject("Sum updated to " + sum + " by " + data.getClientId());
                        } else {
                            objectOutputStream.writeObject("Invalid object. Please send a valid DataObject.");
                        }
                    } catch (ClassNotFoundException e) {
                        objectOutputStream.writeObject("Error: Received unknown object type.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            }
        }
    }


}


