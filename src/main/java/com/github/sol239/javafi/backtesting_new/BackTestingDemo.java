package com.github.sol239.javafi.backtesting_new;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sol239.javafi.postgre.DBHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class BacktestingExecutor {

    /**
     * Suffix for the columns that store the open signals for the strategy
     */
    public static final String STRATEGY_OPEN_COLUMN_SUFFIX = "_stgO_";

    /**
     * Suffix for the columns that store the close signals for the strategy
     */
    public static final String STRATEGY_CLOSE_COLUMN_SUFFIX = "_stgC_";

    public static final String DATE_RESTRICTION_PATH  = "C:\\Users\\david_dyn8g78\\PycharmProjects\\elliptic\\data-preparation\\main\\date.txt";

    public static final String TRADES_RESULT_JSON_PATH = "C:\\Users\\david_dyn8g78\\PycharmProjects\\elliptic\\data-preparation\\main\\trades.json";

    private List<Trade> openedTrades = new ArrayList<>();
    private List<Trade> loosingTrades = new ArrayList<>();
    private List<Trade> winningTrades = new ArrayList<>();

    private Map<Integer, Setup> setups = new HashMap<>();

    private DBHandler db;

    /**
     * Clause that defines the conditions for opening a long trade (closing a short trade)
     * e.g. "WHERE rsi_14_ins_ <= 27"
     */
    public String originalOpenClause;

    /**
     * Clause that defines the conditions for closing a long trade (opening a short trade)
     * e.g. "WHERE rsi_14_ins_ >= 70"
     */
    public String originalCloseClause;
    public BacktestingExecutor(String originalOpenClause, String originalCloseClause) {
        this.originalOpenClause = originalOpenClause;
        this.originalCloseClause = originalCloseClause;

        this.db = new DBHandler();
        this.db.setFetchSize(1000);

    }

    /**
     * Calculates the difference between two dates and returns true if the difference is greater than 'n' seconds.
     * @param dateString1 The first date string in the format 'yyyy-MM-dd HH:mm:ss[.SSSSSS]'
     * @param dateString2 The second date string in the format 'yyyy-MM-dd HH:mm:ss[.SSSSSS]'
     * @param n The number of seconds that the difference must be greater than
     * @return True if the difference between the two dates is greater than 'n' seconds, false otherwise
     */
    public static boolean isDifferenceGreaterThan(String dateString1, String dateString2, long n) {
        // Define the format for the date string that handles both formats (with and without microseconds)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]");

        // Parse the strings to LocalDateTime objects
        LocalDateTime date1 = LocalDateTime.parse(dateString1, formatter);
        LocalDateTime date2 = LocalDateTime.parse(dateString2, formatter);

        // Calculate the difference in seconds between the two dates
        Duration duration = Duration.between(date1, date2);
        long differenceInSeconds = Math.abs(duration.getSeconds());

        // Return true if the difference in seconds is greater than 'n'
        return differenceInSeconds > n;
    }

    /**
     * Returns the name of the column that stores the signals for the strategy.
     * @param open True if the column stores the open signals, false if it stores the close signals
     * @return The name of the column that stores the signals for the strategy
     */
    public String getStrategyColumnName(boolean open) {
        if (open) {
            return "\"" + originalOpenClause + " - " + originalCloseClause + STRATEGY_OPEN_COLUMN_SUFFIX + "\"";
        } else {
            return "\"" + originalOpenClause + " - " + originalCloseClause + STRATEGY_CLOSE_COLUMN_SUFFIX + "\"";
        }
    }

    /**
     * Creates the columns in the database table that store the signals for the strategy.
     * @param tableName The name of the database table
     */
    public void createStrategyColumns(String tableName) {
        String openColumnName = getStrategyColumnName(true);
        String closeColumnName = getStrategyColumnName(false);

        // Creating open/false boolean columns
        String createOpenColumn = """
                ALTER TABLE IF EXISTS %s
                ADD COLUMN IF NOT EXISTS %s BOOLEAN DEFAULT FALSE;
                """.formatted(tableName, openColumnName);

        String createCloseColumn = """
                ALTER TABLE IF EXISTS %s
                ADD COLUMN IF NOT EXISTS %s BOOLEAN DEFAULT FALSE;
                """.formatted(tableName, closeColumnName);

        db.executeQuery(createOpenColumn);
        db.executeQuery(createCloseColumn);
        System.out.println("Strategy columns created.");
    }

    /**
     * Updates the columns in the database table that store the signals for the strategy.
     * @param tableName The name of the database table
     */
    public void updateStrategyColumns(String tableName) {
        String openColumnName = getStrategyColumnName(true);
        String closeColumnName = getStrategyColumnName(false);

        // Update open/close columns based on the original clauses
        String updateOpenColumn = """
                UPDATE %s
                SET %s = TRUE
                %s;
                """.formatted(tableName, openColumnName, originalOpenClause);

        String updateCloseColumn = """
                UPDATE %s
                SET %s = TRUE
                %s;
                """.formatted(tableName, closeColumnName, originalCloseClause);

        db.executeQuery(updateOpenColumn);
        db.executeQuery(updateCloseColumn);
        System.out.println("Strategy columns updated.");
    }

    public void run(String tableName, Setup setup) {

        long t1 = System.currentTimeMillis();

        List<String> lines;
        String dateRestriction;
        try {
            lines = Files.readAllLines(Path.of(DATE_RESTRICTION_PATH));
            dateRestriction = lines.get(0);
        } catch (IOException e) {
            lines = null;
            dateRestriction = "";
        }

        ResultSet rs;
        try {
            rs = this.db.getResultSet(String.format("SELECT * FROM %s %s ORDER BY id", tableName, dateRestriction));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }


        String openX = this.getStrategyColumnName(true).substring(1, this.getStrategyColumnName(true).length() - 1);
        String closeX = this.getStrategyColumnName(false).substring(1, this.getStrategyColumnName(false).length() - 1);

        int skipRows = 1;
        int c = 0;
        try {
            while (rs.next()) {


                c++;

                if (c < skipRows) {
                    continue;
                }

                boolean open = rs.getBoolean(openX);
                boolean close = rs.getBoolean(closeX);
                double closePrice = rs.getDouble("close");
                String now = rs.getString("date");


                // trade closing
                Iterator<Trade> iterator = this.openedTrades.iterator();
                while (iterator.hasNext()) {
                    Trade trade = iterator.next();
                    boolean tradeClosed = false;

                    if (closePrice <= trade.stopPrice) {
                        tradeClosed = true;
                    }

                    // It is generally good to specify that:
                    // - current close must be > than trade.open * (1 + takeProfit)
                    // - current close must be > than trade.open * (1 + fee)
                    if (close) {
                        tradeClosed = true;
                    }

                    if (tradeClosed && closePrice >= trade.openPrice * (1 + setup.fee)) {
                        trade.closeTime = rs.getString("date");
                        trade.closePrice = closePrice;
                        trade.PnL = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * setup.leverage * (1 - setup.fee);
                        if (closePrice >= trade.openPrice) {
                            //System.out.println("WINNING TRADE");
                            winningTrades.add(trade);
                        } else {
                            //System.out.println("LOOSING TRADE");
                            loosingTrades.add(trade);
                        }
                        iterator.remove();

                        double profit = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * setup.leverage;

                        setup.balance += profit;
                        // System.out.println("Balance: " + balance);

                    }

                }

                // long trade opening
                if (open && this.openedTrades.size() < setup.maxTrades && setup.balance - setup.amount >= 0) {
                    if (!this.openedTrades.isEmpty()) {
                        if (isDifferenceGreaterThan(now,this.openedTrades.get(this.openedTrades.size() - 1).openTime, setup.delaySeconds)) {
                            Trade newTrade = new Trade(closePrice, closePrice * (1 + setup.takeProfit), closePrice * (1 - setup.stopLoss), 0, (setup.amount * setup.leverage / closePrice), tableName, rs.getString("date"), "");
                            setup.balance -= setup.amount / closePrice;
                            //System.out.println("Balance: " + balance);
                            this.openedTrades.add(newTrade);


                        }
                    } else  {
                        Trade newTrade = new Trade(closePrice, closePrice * (1 + setup.takeProfit), closePrice * (1 - setup.stopLoss), 0, (setup.amount * setup.leverage / closePrice), tableName, rs.getString("date"), "");
                        setup.balance -= setup.amount / closePrice;
                        //System.out.println("Balance: " + balance);
                        this.openedTrades.add(newTrade);
                    }

                    if (this.openedTrades.size() > setup.maxOpenedTrades) {
                        setup.maxOpenedTrades = this.openedTrades.size();
                    }

                    /*
                    System.out.println("-----------------------------------------");
                    System.out.println("CLOSE PRICE: " + closePrice + " | " + rs.getString("rsi_14_ins_"));
                    System.out.println("Trade opened: ");
                    System.out.println(newTrade);
                    System.out.println("-----------------------------------------");
                    */
                }

                // short trade opening
                // TODO




            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long t2 = System.currentTimeMillis();



        List<Trade> allTrades = evaluateTrades(winningTrades, loosingTrades, setup.amount, setup.leverage, setup.fee);
        // Store into table
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert list of trades to JSON and write to file
            File outputFile = new File(TRADES_RESULT_JSON_PATH);

            objectMapper.writeValue(outputFile, allTrades);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n****************************************");
        System.out.println("Balance: " + String.format("%.2f", setup.balance) + " USD");
        System.out.println("Max opened trades: " + setup.maxOpenedTrades);
        System.out.println("EXECUTION TIME: " + Double.parseDouble(String.valueOf(t2 - t1)) / 1000 + " s");
        System.out.println("****************************************");
    }

    public static List<Trade> evaluateTrades(List<Trade> winningTrades, List<Trade> loosingTrades, double tradeSize, double leverage, double fee) {
        long winningTradesCount = winningTrades.size();
        long loosingTradesCount = loosingTrades.size();
        double winRate = (double) winningTradesCount / (winningTradesCount + loosingTradesCount);

        // Displaying trades

        List<Trade> allTrades = new ArrayList<>();
        allTrades.addAll(winningTrades);
        allTrades.addAll(loosingTrades);


        double percentProfitSum = 0;
        for (Trade trade : allTrades) {
            // print % profit
            double profitPercent = (((trade.closePrice) / trade.openPrice) * 100) - 100;
            //System.out.println(trade);
            System.out.println("Profit: " + String.format("%.2f", profitPercent) + "%");
        }
        double _profit = 0;
        for (Trade trade : allTrades) {
            _profit += (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * leverage * (1 - fee);
        }


        System.out.println("****************************************");
        System.out.println("Winning trades: " + winningTradesCount);
        System.out.println("Loosing trades: " + loosingTradesCount);
        System.out.println("Total trades: " + (winningTradesCount + loosingTradesCount));
        System.out.println("Win rate: " + String.format("%.2f", winRate * 100) + "%");
        System.out.println("Profit: " + String.format("%.2f", _profit) + " USD");
        System.out.println("Trading volume: " + (winningTradesCount + loosingTradesCount) * tradeSize * leverage + " USD");
        System.out.println("****************************************");

        return allTrades;


    }

    public static void main(String[] args) {
        String originalOpenClause = "WHERE rsi_14_ins_ <= 27";
        String originalCloseClause = "WHERE rsi_14_ins_ >= 70";
        String tableName = "btx";

        // ---------------- Setup parameters ----------------
        double balance = 5_000;
        double leverage = 10;
        double fee = (double) 2.5 / 1000;
        double takeProfit = 0.08;
        double stopLoss = 0.02;
        double amount = 500;
        double riskReward = (takeProfit) / (stopLoss);
        int maxTrades = 10;
        int delaySeconds = 3600;
        int maxOpenedTrades = 0;
        Setup setup = new Setup(balance, leverage, fee, takeProfit, stopLoss, amount, riskReward, maxTrades, delaySeconds);
        // -------------------------------------------------


        BacktestingExecutor backtestingExecutor = new BacktestingExecutor(originalOpenClause, originalCloseClause);
        backtestingExecutor.createStrategyColumns(tableName);
        backtestingExecutor.updateStrategyColumns(tableName);

        backtestingExecutor.run(tableName, setup);


    }

}

public class BackTestingDemo {


    public static String getValidColumnName(String clause) {
        String validColumnName = clause.replaceAll(">", "gt");
        validColumnName = validColumnName.replaceAll("<", "lt");
        validColumnName = validColumnName.replaceAll("<=", "lte");
        validColumnName = validColumnName.replaceAll(">=", "glt");
        validColumnName = validColumnName.replaceAll("where", "");
        validColumnName = validColumnName.replaceAll("WHERE", "");
        validColumnName = validColumnName.replaceAll("=", "eq");
        //validColumnName = validColumnName.replaceAll(" ", "_");
        return validColumnName;
    }

    public static String getOriginalClause(String columnName) {
        String originalClause = "WHERE" + columnName;
        originalClause = originalClause.replaceAll("gt", ">");
        originalClause = originalClause.replaceAll("lt", "<");
        originalClause = originalClause.replaceAll("lte", "<=");
        originalClause = originalClause.replaceAll("glt", ">=");
        originalClause = originalClause.replaceAll("eq", "=");
        return originalClause;
    }


    // TODO - Needs to use class where can be some global state saved.
    public static void tradeCloser(List<Trade> openTrades, double closePrice, String now) {

        // close trade
        for (Trade trade : openTrades) {

            // 1) close if trade lifetime is greater than 2 days
            if (isDifferenceGreaterThan(now, trade.openTime, 3600 * 24 * 2)) {
                trade.closeTime = now;
                trade.closePrice = closePrice;
                openTrades.remove(trade);
            }
        }

    }


    public static boolean isDifferenceGreaterThan(String dateString1, String dateString2, long n) {
        // Define the format for the date string that handles both formats (with and without microseconds)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]");

        // Parse the strings to LocalDateTime objects
        LocalDateTime date1 = LocalDateTime.parse(dateString1, formatter);
        LocalDateTime date2 = LocalDateTime.parse(dateString2, formatter);

        // Calculate the difference in seconds between the two dates
        Duration duration = Duration.between(date1, date2);
        long differenceInSeconds = Math.abs(duration.getSeconds());

        // Return true if the difference in seconds is greater than 'n'
        return differenceInSeconds > n;
    }

    public  void main(String[] args) throws SQLException, IOException {


        long t1 = System.currentTimeMillis();

        DBHandler db = new DBHandler();
        db.setFetchSize(1000);


        // --> prostě transformace
        String originalOpenClause = "WHERE rsi_14_ins_ <= 27";
        String originalCloseClause = "WHERE rsi_14_ins_ >= 70";

        String tableName = "btx";

        String openColumnName = "\"" + originalOpenClause + " - " + originalCloseClause + "_stgO_\"";
        String closeColumnName = "\"" + originalOpenClause + " - " + originalCloseClause + "_stgC_\"";

        System.out.println(openColumnName);
        System.out.println(closeColumnName);

        // Creating open/false boolean columns
        String createOpenColumn = """
                ALTER TABLE IF EXISTS %s
                ADD COLUMN IF NOT EXISTS %s BOOLEAN DEFAULT FALSE;
                """.formatted(tableName, openColumnName);

        String createCloseColumn = """
                ALTER TABLE IF EXISTS %s
                ADD COLUMN IF NOT EXISTS %s BOOLEAN DEFAULT FALSE;
                """.formatted(tableName, closeColumnName);

        db.executeQuery(createOpenColumn);
        db.executeQuery(createCloseColumn);
        System.out.println("Strategy columns created.");
        // -------------------------------------------------


        // SKIP IF ALREADY CREATED
/*
        // Update open/close columns based on the original clauses
        String updateOpenColumn = """
                UPDATE %s
                SET %s = TRUE
                %s;
                """.formatted(tableName, openColumnName, originalOpenClause);

        String updateCloseColumn = """
                UPDATE %s
                SET %s = TRUE
                %s;
                """.formatted(tableName, closeColumnName, originalCloseClause);

        db.executeQuery(updateOpenColumn);
        db.executeQuery(updateCloseColumn);
        System.out.println("Strategy columns updated.");
        */

        // ---------------------------------------------------------------


        // execute trades based on signals
        List<Trade> trades = new ArrayList<>();
        List<Trade> winningTrades = new ArrayList<>();
        List<Trade> loosingTrades = new ArrayList<>();


        String dateWherePath = "C:\\Users\\david_dyn8g78\\PycharmProjects\\elliptic\\data-preparation\\main\\date.txt";
        List<String> lines = Files.readAllLines(Path.of(dateWherePath));

        String fromDate = lines.get(0);
        ResultSet rs = db.getResultSet(String.format("SELECT * FROM %s %s ORDER BY id", tableName, fromDate));


        double balance = 5_000;
        double leverage = 10;
        double fee = (double) 2.5 / 1000;
        double takeProfit = 0.08;
        double stopLoss = 0.02;
        double amount = 500;
        double riskReward = (takeProfit) / (stopLoss);
        int maxTrades = 10;
        //int maxTrades = (int) ((int) balance / amount);
        int delaySeconds = 3600;
        int maxOpenedTrades = 0;

        System.out.println("\n****************************************");
        System.out.println("SETUP:");
        System.out.println("Take profit: " + takeProfit);
        System.out.println("Stop loss: " + stopLoss);
        System.out.println("Risk Reward: " + riskReward);
        System.out.println("Trade size: " + amount + " USD");
        System.out.println("Max trades: " + maxTrades);
        System.out.println("Leverage: " + leverage);
        System.out.println("Interval: " + fromDate.split(">= ")[1].split(" AND")[0] + "-" + fromDate.split("<= ")[1]);
        System.out.println("****************************************\n");

        String openX = openColumnName.substring(1, openColumnName.length() - 1);
        String closeX = closeColumnName.substring(1, closeColumnName.length() - 1);

        int skipRows = 1;
        int c = 0;
        try {
            while (rs.next()) {


                c++;

                if (c < skipRows) {
                    continue;
                }

                boolean open = rs.getBoolean(openX);
                boolean close = rs.getBoolean(closeX);
                double closePrice = rs.getDouble("close");
                String now = rs.getString("date");


                // trade closing
                Iterator<Trade> iterator = trades.iterator();
                while (iterator.hasNext()) {
                    Trade trade = iterator.next();
                    boolean tradeClosed = false;

                    if (closePrice <= trade.stopPrice) {
                        tradeClosed = true;
                    }

                    // It is generally good to specify that:
                    // - current close must be > than trade.open * (1 + takeProfit)
                    // - current close must be > than trade.open * (1 + fee)
                    if (close) {
                        tradeClosed = true;
                    }

                    if (tradeClosed && closePrice >= trade.openPrice * (1 + fee)) {
                        trade.closeTime = rs.getString("date");
                        trade.closePrice = closePrice;
                        trade.PnL = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * leverage * (1 - fee);
                        if (closePrice >= trade.openPrice) {
                            //System.out.println("WINNING TRADE");
                            winningTrades.add(trade);
                        } else {
                            //System.out.println("LOOSING TRADE");
                            loosingTrades.add(trade);
                        }
                        iterator.remove();

                        double profit = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * leverage;

                        balance += profit;
                        // System.out.println("Balance: " + balance);

                    }

                }

                // long trade opening
                if (open && trades.size() < maxTrades && balance - amount >= 0) {
                    if (!trades.isEmpty()) {
                        if (isDifferenceGreaterThan(now,trades.get(trades.size() - 1).openTime, delaySeconds)) {
                            Trade newTrade = new Trade(closePrice, closePrice * (1 + takeProfit), closePrice * (1 - stopLoss), 0, (amount * leverage / closePrice), tableName, rs.getString("date"), "");
                            balance -= amount / closePrice;
                            //System.out.println("Balance: " + balance);
                            trades.add(newTrade);


                        }
                    } else  {
                        Trade newTrade = new Trade(closePrice, closePrice * (1 + takeProfit), closePrice * (1 - stopLoss), 0, (amount * leverage / closePrice), tableName, rs.getString("date"), "");
                        balance -= amount / closePrice;
                        //System.out.println("Balance: " + balance);
                        trades.add(newTrade);
                    }

                    if (trades.size() > maxOpenedTrades) {
                        maxOpenedTrades = trades.size();
                    }

                    /*
                    System.out.println("-----------------------------------------");
                    System.out.println("CLOSE PRICE: " + closePrice + " | " + rs.getString("rsi_14_ins_"));
                    System.out.println("Trade opened: ");
                    System.out.println(newTrade);
                    System.out.println("-----------------------------------------");
                    */
                }

                // short trade opening
                // TODO




            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long t2 = System.currentTimeMillis();



        List<Trade> allTrades = evaluateTrades(winningTrades, loosingTrades, amount, leverage, fee);
        // Store into table
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert list of trades to JSON and write to file
            File outputFile = new File("C:\\Users\\david_dyn8g78\\PycharmProjects\\elliptic\\data-preparation\\main\\trades.json");

            objectMapper.writeValue(outputFile, allTrades);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\n****************************************");
        System.out.println("Balance: " + String.format("%.2f", balance) + " USD");
        System.out.println("Max opened trades: " + maxOpenedTrades);
        System.out.println("EXECUTION TIME: " + Double.parseDouble(String.valueOf(t2 - t1)) / 1000 + " s");
        System.out.println("****************************************");

        db.closeConnection();
    }

    public static List<Trade> evaluateTrades(List<Trade> winningTrades, List<Trade> loosingTrades, double tradeSize, double leverage, double fee) {
        long winningTradesCount = winningTrades.size();
        long loosingTradesCount = loosingTrades.size();
        double winRate = (double) winningTradesCount / (winningTradesCount + loosingTradesCount);

        // Displaying trades

        List<Trade> allTrades = new ArrayList<>();
        allTrades.addAll(winningTrades);
        allTrades.addAll(loosingTrades);


        double percentProfitSum = 0;
        for (Trade trade : allTrades) {
            // print % profit
            double profitPercent = (((trade.closePrice) / trade.openPrice) * 100) - 100;
            //System.out.println(trade);
            System.out.println("Profit: " + String.format("%.2f", profitPercent) + "%");
        }
        double _profit = 0;
        for (Trade trade : allTrades) {
            _profit += (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * leverage * (1 - fee);
        }


        System.out.println("****************************************");
        System.out.println("Winning trades: " + winningTradesCount);
        System.out.println("Loosing trades: " + loosingTradesCount);
        System.out.println("Total trades: " + (winningTradesCount + loosingTradesCount));
        System.out.println("Win rate: " + String.format("%.2f", winRate * 100) + "%");
        System.out.println("Profit: " + String.format("%.2f", _profit) + " USD");
        System.out.println("Trading volume: " + (winningTradesCount + loosingTradesCount) * tradeSize * leverage + " USD");
        System.out.println("****************************************");

        return allTrades;


    }


}
