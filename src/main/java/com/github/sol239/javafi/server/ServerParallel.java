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
    private CmdController cc;


    public ServerParallel() {
        cc = new CmdController();
    }


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

    private static DataObject operationSelector(int id, String cmd) {

        DataObject response = null;

        switch (id) {
            case 1 -> {
                response = insertToDB(cmd);
            }
            case 2 -> {
                response = runConsoleOperation(cmd);
            }
        }

        return response;
    }

    private static DataObject insertToDB(String cmd) {

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

            DataObject errorObject = new DataObject(400, "server", "Invalid command format");

            return errorObject;
        }

        try {
            csvPath = cmdArray[1];
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());

            DataObject errorObject = new DataObject(400, "server", "Invalid command format");

            return errorObject;
        }

        System.out.println("Operation: insertToDB");
        System.out.println(tableName);
        System.out.println(csvPath);

        try {
            db.insertCsvData(tableName, csvPath);
            DataObject dataObject = new DataObject(200, "server", "Data inserted to " + tableName);
            return dataObject;

        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());

            DataObject errorObject = new DataObject(400, "server", "Error inserting data to " + tableName);
            return errorObject;

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
    public static DataObject runConsoleOperation(String cmd) {
        String[] cmdArray = cmd.split(" X ");

        System.out.println(Arrays.toString(cmdArray));

        String operationName = "";
        String tables = "";
        String operationString = "";

        try {
            operationName = cmdArray[0].trim();
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());

            DataObject errorObject = new DataObject(400, "server", "Invalid command format");

            return errorObject;
        }

        if (cmdArray.length > 1) {
            try {
                tables = cmdArray[1];
            } catch (Exception e) {
                System.out.println("FAIL - " + e.getMessage());

                DataObject errorObject = new DataObject(400, "server", "Invalid command format");

                return errorObject;
            }

            try {
                operationString = cmdArray[2];
            } catch (Exception e) {
                System.out.println("FAIL - " + e.getMessage());

                DataObject errorObject = new DataObject(400, "server", "Invalid command format");

                return errorObject;
            }
        }
        /*
        System.out.println("Operation: " + operationName);
        System.out.println("Tables: " + tables);
        System.out.println("Operation String: " + operationString);
*/

        switch (operationName) {
            // checks whether the client is still connected to the server
            case "cn" -> {
                DataObject dataObject = new DataObject(200, "server", "Connection Open");
                return dataObject;
            }

            // checks if it is possible to connect to the database
            case "db" -> {
                DBHandler db = new DBHandler();
                try {
                    db.connect();
                    if (db.conn != null) {
                        DataObject dataObject = new DataObject(200, "server", "Database connection successful");
                        return dataObject;
                    } else {
                        DataObject errorObject = new DataObject(400, "server", "Database connection failed");
                        return errorObject;
                    }

                } catch (Exception e) {
                    DataObject errorObject = new DataObject(400, "server", "Database connection failed");
                    return errorObject;
                } finally {
                    db.closeConnection();
                }
            }

            // insert csv into db
            case "in" -> {
                String dbInsertString = tables + " X " + operationString;
                return insertToDB(dbInsertString);
            }

            // store operation for instruments
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

                            System.out.print(method.getName() + "(");
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


                DataObject dataObject = new DataObject(200, "server", "Operation completed");
                return dataObject;

            }

            // backtest operation
            case "bt" -> {
                String[] tableArray = tables.split(",");

                for (String tableName : tableArray) {
                    String strategyName = cmdArray[2];
                    String buyClause = extractStrategyClause("BUY", cmdArray[3]);
                    String sellClause = extractStrategyClause("SELL", cmdArray[4]);

                    System.out.println("Buy Clause: " + buyClause);
                    System.out.println("Sell Clause: " + sellClause);

                    String sellStrategyName = strategyName + "_sell_" + tableName;
                    String buyStrategyName = strategyName + "_buy_" + tableName;



                    String sellSql = getSqlStrategyString(tableName, sellStrategyName, sellClause);
                    String buySql = getSqlStrategyString(tableName, buyStrategyName, buyClause);

                    System.out.println("Buy SQL: \n" + buySql);

                    SqlHandler.executeQuery(sellSql);
                    SqlHandler.executeQuery(buySql);
                }

                DataObject dataObject = new DataObject(200, "server", "Operation completed");
                return dataObject;


            }
        }

        DataObject errorObject = new DataObject(400, "server", "Operation not found");
        return errorObject;
    }

    public static String getSqlStrategyString(String tableName, String strategyName, String strategyClause) {
        String sql = String.format(
                "alter table %s " +
                        "drop column if exists %s; " +

                        "ALTER TABLE %s " +
                        "ADD %s boolean default false; " +

                        "update %s " +
                        "set %s = true " +
                        "where %s; ",
                tableName,
                strategyName,
                tableName,
                strategyName,
                tableName,
                strategyName,
                strategyClause
        );

        return sql;
    }

    public static String extractStrategyClause(String type, String clause) {
        String x = clause.split(type)[1];
        x = x.substring(1, x.length() - 1);
        return x;
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
                            DataObject response = operationSelector(id, cmd);

                            // Send the response back to the client
                            objectOutputStream.writeObject(response);

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


