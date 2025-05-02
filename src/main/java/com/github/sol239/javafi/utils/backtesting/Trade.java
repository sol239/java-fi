package com.github.sol239.javafi.utils.backtesting;

/**
 * A Class to represent a trade.
 */
public class Trade {

    /**
     * The price at which the trade was opened.
     */
    public double openPrice;

    /**
     * The price at which the trade is desired to be closed.
     */
    public double takePrice;

    /**
     * The price at which the trade is desired to be stopped. Stop price should be lower than the open and take prices.
     */
    public double stopPrice;

    /**
     * The price at which the trade was closed.
     */
    public double closePrice;

    /**
     * The amount of asset traded.
     */
    public double amount;

    /**
     * The name of the asset traded.
     */
    public String assetName;

    /**
     * The time at which the trade was opened.
     */
    public String openTime;

    /**
     * The time at which the trade was closed.
     */
    public String closeTime;

    /**
     * The profit and loss of the trade.
     */
    public double PnL;

    /**
     * The strategy used for the trade.
     */
    public Strategy strategy;

    /**
     * Constructor for Trade
     * @param takePrice the price at which the trade is desired to be closed.
     * @param stopPrice the price at which the trade is desired to be stopped. Stop price should be lower than the open and take prices.
     * @param closePrice the price at which the trade was closed.
     * @param amount the amount of asset traded.
     * @param assetName the name of the asset traded.
     * @param openTime the time at which the trade was opened.
     * @param closeTime the time at which the trade was closed.
     * @param strategy the strategy used for the trade.
     */
    public Trade(double openPrice, double takePrice, double stopPrice, double closePrice, double amount, String assetName, String openTime, String closeTime, Strategy strategy) {
        this.openPrice = openPrice;
        this.takePrice = takePrice;
        this.stopPrice = stopPrice;
        this.closePrice = closePrice;
        this.amount = amount;
        this.assetName = assetName;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.strategy = strategy;
    }

    /**
     * Returns the string representation of the trade.
     * @return String representation of the trade.
     */
    public String toString() {
        return "open = " + openPrice + "\n" +
                "close = " + closePrice + "\n" +
                "take = " + takePrice + "\n" +
                "stop = " + stopPrice + "\n" +
                "amount = " + amount + "\n" +
                "profit = " + String.format("%.2f", PnL) + "\n" +
                "assetName = " + assetName + "\n" +
                "openTime = " + openTime + "\n" +
                "closeTime = " + closeTime + "\n";
    }
}
