package com.github.sol239.javafi.utils.backtesting;

/**
 * Class to represent a trade.
 */
public class Trade {
    public double openPrice;

    public double takePrice;
    public double stopPrice;
    public double closePrice;

    public double amount;
    public String assetName;
    public String openTime;
    public String closeTime;
    public double PnL;

    public Trade(double openPrice, double takePrice, double stopPrice, double closePrice, double amount, String assetName, String openTime, String closeTime) {
        this.openPrice = openPrice;
        this.takePrice = takePrice;
        this.stopPrice = stopPrice;
        this.closePrice = closePrice;
        this.amount = amount;
        this.assetName = assetName;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    @Override
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
