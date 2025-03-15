package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.backtesting.Strategy;
import com.github.sol239.javafi.backtesting.Trade;
import com.github.sol239.javafi.instruments.SqlHandler;
import com.github.sol239.javafi.postgre.DBHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class BtCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "bt";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: bt [OPTION]...\n" +
                getParameters() + "\n" +
                "The command executes back-testing logic.\n";
    }

    /**
     * Method to get the parameters of the command.
     *
     * @return parameters of the command
     */
    @Override
    public String getParameters() {
        return "Options:\n" +
                "  -h, --help\n" +
                "  -t, --tables=TABLES\n" +
                "  -s, --strategy=STRATEGY\n";
    }

    /**
     * Method to run the command.
     *
     * @param args  arguments
     * @param flags flags
     * @return result
     */
    @Override
    public DataObject run(List<String> args, List<String> flags) {


        String tables = "";
        String buyClause = "";
        String sellClause = "";
        String strategyName = "";

        for (String flag : flags) {
            if (flag.startsWith("-h") || flag.startsWith("--help")) {
                return new DataObject(200, "server", getDescription());
            } else if (flag.startsWith("-t=") || flag.startsWith("--tables=")) {
                tables = flag.split("=")[1];
            } else if (flag.startsWith("-s=") || flag.startsWith("--strategy=")) {
                strategyName = flag.split("=")[1];
            }
        }

        for (String arg : args) {
            if (arg.startsWith("BUY")) {
                buyClause = extractStrategyClause("BUY", arg);
            } else if (arg.startsWith("SELL")) {
                sellClause = extractStrategyClause("SELL", arg);
            }
        }


        String[] tableArray = tables.split(",");

        for (String tableName : tableArray) {

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
}
