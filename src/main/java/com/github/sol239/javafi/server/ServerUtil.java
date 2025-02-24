package com.github.sol239.javafi.server;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.backtesting.Strategy;
import com.github.sol239.javafi.backtesting.Trade;
import com.github.sol239.javafi.instruments.SqlHandler;
import com.github.sol239.javafi.instruments.SqlInstruments;
import com.github.sol239.javafi.postgre.DBHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServerUtil {


    /**
     * Method used to check client-server connection.
     * Can be called with 'cn' client console command.
     *
     * @return DataObject with the command "Connection Open"
     */
    public static DataObject cnCommand() {
        DataObject dataObject = new DataObject(200, "server", "Connection Open");
        return dataObject;
    }

    /**
     * Method used to check database connection.
     * Can be called with 'db' client console command.
     *
     * @return DataObject with the command "Connection Closed"
     */
    public static DataObject dbCommand() {
        DBHandler db;
        try {
            db = new DBHandler();
            if (db.conn != null) {
                DataObject dataObject = new DataObject(200, "server", "Database connection successful");
                db.closeConnection();
                return dataObject;
            } else {
                DataObject errorObject = new DataObject(400, "server", "Database connection failed");
                db.closeConnection();
                return errorObject;
            }

        } catch (Exception e) {
            DataObject errorObject = new DataObject(400, "server", "Database connection failed");
            return errorObject;
        }
    }

    /**
     * Method used to delete a table from the database.
     *
     * @param tables String with table names separated by commas.
     * @return DataObject with the result of the operation.
     */
    public static DataObject delCommand(String tables) {
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

    /**
     * Method used to delete all instruments and strategies columns from all tables.
     *
     * @return DataObject with the result of the operation.
     */
    public static DataObject cleanCommand() {
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

    /**
     * Method used to create columns for given strategies in corresponding tables.
     *
     * @param tables          String with table names separated by commas.
     * @param operationString String with instrument names and arguments separated by commas.
     * @return DataObject with the result of the operation.
     */
    public static DataObject stCommand(String tables, String operationString) {
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

                    Parameter[] parameters = method.getParameters();
                    Object[] arguments = new Object[parameters.length];

                    for (int i = 0; i < parameters.length; i++) {
                        arguments[i] = convertToType(_arguments.get(i), parameters[i].getType());
                    }

                    for (int i = 0; i < arguments.length - 1; i++) {
                        System.out.print(arguments[i] + ", ");
                    }

                    String result = "";

                    // invoke
                    try {
                        result = (String) method.invoke(obj, arguments);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SqlHandler.executeQuery(result);
                }
            }
        }
        DataObject dataObject = new DataObject(200, "server", "Operation completed");
        return dataObject;
    }

    /**
     * Convert the input string to the specified type.
     * It's used for reflection to convert the string input to the correct type.
     *
     * @param input the input string
     * @param type  the type to convert to
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
     * Method used to run back-testing (bt) on the given tables with the given strategy.
     *
     * @param tables          String with table names separated by commas.
     * @param operationString String with the strategy name and arguments.
     * @param cmdArray        Array with the buy and sell clauses.
     * @return DataObject with the result of the operation.
     */
    public static DataObject btCommand(String tables, String operationString, String[] cmdArray) {
        String[] tableArray = tables.split(",");

        for (String tableName : tableArray) {
            String strategyName = cmdArray[2];
            String buyClause = extractStrategyClause("BUY", cmdArray[3]);
            String sellClause = extractStrategyClause("SELL", cmdArray[4]);

            String sellStrategyName = strategyName + "_sell_" + tableName;
            String buyStrategyName = strategyName + "_buy_" + tableName;

            String sellSql = DBHandler.getSqlStrategyString(tableName, sellStrategyName, sellClause);
            String buySql = DBHandler.getSqlStrategyString(tableName, buyStrategyName, buyClause);

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
        }
        DataObject dataObject = new DataObject(200, "server", "Operation completed");
        return dataObject;
    }

    /**
     * Method used to get exact datetime from the ResultSet.
     * .getDate operation returns only the date without the time.
     *
     * @param rs         ResultSet
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

    // TODO: Needs to be reworked so long and short trades are possible.
    public static String extractStrategyClause(String type, String clause) {
        String x = clause.split(type)[1];
        x = x.substring(1, x.length() - 1);
        return x;
    }

    public static DataObject dbcCommand(String[] cmdArray) {
        String operationName = cmdArray[0];
        String dbUrl = cmdArray[1];
        String username = cmdArray[2];
        String password = cmdArray[3];

        final String dbConfigFileName = "db_config";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbConfigFileName))) {
            writer.write(dbUrl);
            writer.newLine();
            writer.write(username);
            writer.newLine();
            writer.write(password);
            return new DataObject(200, "server", "DB Config updated succesfully.");
        } catch (IOException e) {
            e.printStackTrace();
            return new DataObject(400, "server", "DB Config update failed.");

        }

    }
}
