package com.github.sol239.javafi.server;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.backtesting.Strategy;
import com.github.sol239.javafi.backtesting.Trade;
import com.github.sol239.javafi.instruments.SqlHandler;
import com.github.sol239.javafi.instruments.SqlInstruments;
import com.github.sol239.javafi.postgre.DBHandler;
import com.github.sol239.javafi.utils.ClientServerUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
     * Main method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            serverSocket.setReuseAddress(true);
            ExecutorService executorService = java.util.concurrent.Executors.newCachedThreadPool();

            while (true) {
                Socket socket = serverSocket.accept();
                clients.add(socket);
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress());
                // Handle each client in a separate thread
                executorService.execute(new ClientHandler(socket));
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    /**
     * Operation selector based on the number received from client.
     * Currently only one operation is implemented since all commands
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

    // TODO: It should be in a DBHandler Class.
    private static DataObject insertToDB(String cmd) {

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

    // TODO: Method clean up is needed.
    public static DataObject runConsoleOperation(String cmd) {
        String[] cmdArray = cmd.split(" X ");

        System.out.println(Arrays.toString(cmdArray));

        String operationName = "";
        String tables = "";
        String operationString = "";

        operationName = cmdArray[0].trim();
        try {
            tables = cmdArray[1];
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
        }
        try {
            operationString = cmdArray[2];
        } catch (Exception e) {
            System.out.println("FAIL - " + e.getMessage());
        }


        System.out.println("Operation: " + operationName);
        System.out.println("Tables: " + tables);
        System.out.println("Operation String: " + operationString);

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

            // deletes the table from db
            case "del" -> {
                DBHandler db = new DBHandler();
                try {
                    db.connect();

                    for (String table : tables.split(",")) {
                        db.deleteTable(table);
                    }

                    DataObject dataObject = new DataObject(200, "server", "Table deleted");
                    return dataObject;
                } catch (Exception e) {
                    DataObject errorObject = new DataObject(400, "server", "Table deletion failed");
                    return errorObject;
                } finally {
                    db.closeConnection();
                }
            }

            // removes everything from the database except the schema
            case "clean" -> {
                DBHandler db = new DBHandler();
                try {
                    db.connect();
                    List<String> tablesToClean = db.getAllTables();
                    for (String table : tablesToClean) {
                        db.clean(table);
                    }

                    DataObject dataObject = new DataObject(200, "server", "Database cleaned");
                    return dataObject;
                } catch (Exception e) {
                    DataObject errorObject = new DataObject(400, "server", "Database cleaning failed");
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
                    String symbol = x.split(":")[0];
                    String[] arguments = x.split(":")[1].split(";");

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

                    DBHandler db = new DBHandler();
                    db.setFetchSize(100);
                    String sql = String.format("SELECT * FROM %s ORDER BY date", tableName);

                    ResultSet rs = db.getResultSet(sql);

                    Strategy strategy = new Strategy(strategyName, 1_000_000, 0.08, 0.03);

                    List<Trade> trades = new Stack<>();
                    List<Trade> successfulTrades = new ArrayList<>();
                    List<Trade> unsuccessfulTrades = new ArrayList<>();

                    double successfullTrades = 0;
                    double unsuccessfullTrades = 0;

                    try {
                        while (rs.next()) {
                            boolean buySignal = rs.getBoolean(buyStrategyName);
                            boolean sellSignal = rs.getBoolean(sellStrategyName);
                            double close = rs.getDouble("close");

                            // Use an Iterator to safely remove trades
                            Iterator<Trade> iterator = trades.iterator();
                            while (iterator.hasNext()) {
                                Trade trade = iterator.next();
                                if (trade.takeProfit >= close) {
                                    strategy.balance += trade.takeProfit;
                                    iterator.remove(); // Safe removal
                                    successfullTrades++;
                                    trade.closeDate = formatCloseDate(rs, "date");

                                    successfulTrades.add(trade);
                                } else if (trade.stopLoss <= close) {
                                    strategy.balance -= trade.stopLoss;
                                    iterator.remove(); // Safe removal
                                    unsuccessfullTrades++;
                                    trade.closeDate = formatCloseDate(rs, "date");

                                    unsuccessfulTrades.add(trade);
                                }
                            }

                            if (buySignal) {
                                Trade trade = new Trade(close, close * (1 + strategy.profit), close * (1 - strategy.loss));
                                trade.openDate = formatCloseDate(rs, "date");
                                trades.add(trade);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        db.closeConnection();
                        System.out.println("\nStrategy: " + strategy.name);
                        System.out.println("Balance: " + strategy.balance);
                        System.out.println("Successfull Trades: " + successfullTrades);
                        System.out.println("Unsuccessfull Trades: " + unsuccessfullTrades);
                        System.out.println("Win rate: " + (successfullTrades / (successfullTrades + unsuccessfullTrades)));
                    }

                    // add trade date into the db:
                    // - boolean
                    // - where date of trade = date of close
                    List<String> datesToUpdate = new ArrayList<>();
                    for (Trade trade : successfulTrades) {
                        datesToUpdate.add(trade.openDate.toString());
                        //System.out.println(trade);
                    }


                    String createSql = String.format("ALTER TABLE %s ADD COLUMN IF NOT EXISTS %s boolean default false;", tableName, buyStrategyName + "_trade");
                    SqlHandler.executeQuery(createSql);


                    /*
                    for (Trade trade : successfulTrades) {
                        String date = trade.date.toString();
                        String createSql = String.format("ALTER TABLE %s ADD COLUMN IF NOT EXISTS %s boolean default false", tableName, buyStrategyName + "_trade");
                        String updateSql = String.format("\nUPDATE %s SET %s = true WHERE date = '%s'", tableName, buyStrategyName + "_trade", date);
                        SqlHandler.executeQuery(createSql + updateSql);
                    }
                    */


                }


                DataObject dataObject = new DataObject(200, "server", "Operation completed");
                return dataObject;


            }
        }

        DataObject errorObject = new DataObject(400, "server", "Operation not found");
        return errorObject;
    }

    /**
     * Method used to get exact datetime from the ResultSet.
     * .getDate operation returns only the date without the time.
     * @param rs ResultSet
     * @param columnName Column name
     * @return String representation of the date
     */
    public static String formatCloseDate(ResultSet rs, String columnName) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        try {
            Timestamp timestamp = rs.getTimestamp(columnName);
            if (timestamp != null) {
                return simpleDateFormat.format(timestamp);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO: Separation of sql logic from the server logic.
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

    // TODO: Needs to be reworked so long and short trades are possible.
    public static String extractStrategyClause(String type, String clause) {
        String x = clause.split(type)[1];
        x = x.substring(1, x.length() - 1);
        return x;
    }

    /**
     * Convert the input string to the specified type.
     * It's used for reflection to convert the string input to the correct type.
     * @param input the input string
     * @param type the type to convert to
     * @return the object of the specified type
     */
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


