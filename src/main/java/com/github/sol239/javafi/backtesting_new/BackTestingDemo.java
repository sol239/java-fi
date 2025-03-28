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

    public Setup setup;

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
                String closeTime = rs.getString("date");
                String now = rs.getString("date");

                // trade closing
                Iterator<Trade> iterator = this.openedTrades.iterator();
                while (iterator.hasNext()) {
                    Trade trade = iterator.next();
                    boolean tradeClosed = false;

                    if (closePrice <= trade.stopPrice) {

                        // System.out.println("LIQUIDATION");

                        trade.closeTime = closeTime;
                        trade.closePrice = closePrice;
                        trade.PnL = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * setup.leverage * (1 - setup.fee);
                        if (closePrice >= trade.openPrice) {
                            winningTrades.add(trade);
                        } else {
                            loosingTrades.add(trade);
                        }
                        iterator.remove();

                        double profit = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * setup.leverage;
                        setup.balance += profit;
                        continue;
                    }

                    // It is generally good to specify that:
                    // - current close must be > than trade.open * (1 + takeProfit)
                    // - current close must be > than trade.open * (1 + fee)
                    if (close) {
                        //System.out.println("CLOSE CONDITION");

                        trade.closeTime = closeTime;
                        trade.closePrice = closePrice;
                        trade.PnL = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * setup.leverage * (1 - setup.fee);
                        if (closePrice >= trade.openPrice) {
                            winningTrades.add(trade);
                        } else {
                            loosingTrades.add(trade);
                        }
                        iterator.remove();

                        double profit = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * setup.leverage;
                        setup.balance += profit;
                    }
                }

                Iterator<Trade> openedTradesIterator = this.openedTrades.iterator();
                this.closeTrades(openedTradesIterator, setup, closePrice, closeTime);
                // long trade opening
                if (open && this.openedTrades.size() < setup.maxTrades && setup.balance - setup.amount >= 0) {
                    if (!this.openedTrades.isEmpty()) {
                        if (isDifferenceGreaterThan(now,this.openedTrades.get(this.openedTrades.size() - 1).openTime, setup.delaySeconds)) {
                            Trade newTrade = new Trade(closePrice, closePrice * (1 + setup.takeProfit), closePrice * (1 - setup.stopLoss), 0, (setup.amount * setup.leverage / closePrice), tableName, rs.getString("date"), "");
                            setup.balance -= setup.amount / closePrice;
                            this.openedTrades.add(newTrade);
                            //System.out.println(newTrade);


                        }
                    } else  {
                        Trade newTrade = new Trade(closePrice, closePrice * (1 + setup.takeProfit), closePrice * (1 - setup.stopLoss), 0, (setup.amount * setup.leverage / closePrice), tableName, rs.getString("date"), "");
                        setup.balance -= setup.amount / closePrice;
                        this.openedTrades.add(newTrade);
                        //System.out.println(newTrade);

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
                // TODO: SHORT TRADES
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        long t2 = System.currentTimeMillis();


        // Saving trades to JSON
        List<Trade> allTrades = evaluateTrades(winningTrades, loosingTrades, setup.amount, setup.leverage, setup.fee);
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert list of trades to JSON and write to file
            File outputFile = new File(TRADES_RESULT_JSON_PATH);
            objectMapper.writeValue(outputFile, allTrades);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // -------------------------------------------------

        System.out.println("\n****************************************");
        System.out.println("Balance: " + String.format("%.2f", setup.balance) + " USD");
        System.out.println("Max opened trades: " + setup.maxOpenedTrades);
        System.out.println("EXECUTION TIME: " + Double.parseDouble(String.valueOf(t2 - t1)) / 1000 + " s");
        System.out.println("Trade Counts: " + this.openedTrades.size() + " | " + winningTrades.size() + " | " + loosingTrades.size());
        System.out.println("****************************************");

        this.setup = setup;
    }

    public void closeTrades(Iterator<Trade> iterator, Setup setup, double closePrice, String closeTime) {


        //System.out.println("closeTrades");

        while (iterator.hasNext()) {
            Trade trade = iterator.next();
            // 1) close if trade lifetime is greater than 2 days
            //System.out.println(closeTime);
            //System.out.println(trade.openTime);
            if (isDifferenceGreaterThan(closeTime, trade.openTime, 3600 * 12
            )) {
                System.out.println("CLOSING - TIME LIMIT");
                trade.closeTime = closeTime;
                trade.closePrice = closePrice;
                trade.PnL = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * setup.leverage * (1 - setup.fee);
                if (closePrice >= trade.openPrice) {
                    winningTrades.add(trade);
                } else {
                    loosingTrades.add(trade);
                }
                iterator.remove();
                double profit = (trade.closePrice * trade.amount - trade.openPrice * trade.amount) * setup.leverage;
                setup.balance += profit;
            }
        }
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
            //System.out.println("Profit: " + String.format("%.2f", profitPercent) + "%");
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
        double balance = 10000;
        double leverage = 5;
        double fee = (double) 2.5 / 1000;
        double takeProfit = 0.02;
        double stopLoss = 0.015;
        double amount = 1000;
        double riskReward = (takeProfit) / (stopLoss);
        int maxTrades = 5;
        int delaySeconds = 3600 * 3;
        int maxOpenedTrades = 0;
        Setup setup = new Setup(balance, leverage, fee, takeProfit, stopLoss, amount, riskReward, maxTrades, delaySeconds);
        // -------------------------------------------------


        BacktestingExecutor backtestingExecutor = new BacktestingExecutor(originalOpenClause, originalCloseClause);
        backtestingExecutor.setup = setup;
        backtestingExecutor.createStrategyColumns(tableName);

        // does not have to be called if the columns were already updated
        // backtestingExecutor.updateStrategyColumns(tableName);

        System.out.println("****************************************");
        System.out.println(backtestingExecutor.setup);
        System.out.println("****************************************\n");


        backtestingExecutor.run(tableName, setup);


    }

}
