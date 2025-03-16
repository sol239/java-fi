package com.github.sol239.javafi.backtesting;

import java.util.*;

public class TradingStrategy {

    // chceme long a short trady
    // - price
    // - stop loss
    // - take profit
    // - (amount)
    // --------------------------
    // - Risk-Reward ratio
    // - P/L

    /**
     * The name of the table - e.g. btc.
     */
    private String tableName;

    private Set<String> columns;

    /**
     * The name of the trading strategy.
     */
    private String name;

    private String openDate;
    private String closeDate;

    private double openPrice;
    private double closePrice;

    private double open;
    private double close;
    private double stopLoss;
    private double takeProfit;
    private double amount;

    private double riskRewardRatio;
    private double profitLoss;

    private HashMap<String, Double> openMap;
    private HashMap<String, Double> closeMap;

    public TradingStrategy(String tableName) {
        this.tableName = tableName;
        this.openMap = new HashMap<>();
        this.closeMap = new HashMap<>();

        this.columns = new HashSet<>();
    }

    public void addColumn(String column) {
        columns.add(column);
    }

    public void addOpen(String key, double value) {
        openMap.put(key, value);
        columns.add(key);
    }

    public void addClose(String key, double value) {
        closeMap.put(key, value);
        columns.add(key);
    }

    public String getName() {
        return name;
    }

    public HashMap<String, Double> getOpenMap() {
        return openMap;
    }

    public HashMap<String, Double> getCloseMap() {
        return closeMap;
    }

    public Set<String> getColumns() {
        columns.addAll(openMap.keySet());
        columns.addAll(closeMap.keySet());
        return columns;
    }

}
