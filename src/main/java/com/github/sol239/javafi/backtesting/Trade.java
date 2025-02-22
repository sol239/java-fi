package com.github.sol239.javafi.backtesting;

import java.sql.Date;

public class Trade {

    public double open;
    public double takeProfit;
    public double stopLoss;

    public String openDate;
    public String closeDate;

    public Trade(double open, double takeProfit, double stopLoss) {
        this.open = open;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
    }

    @Override
    public String toString() {
        return String.format(
                "\n" +
                "-------------------------\n" +
                "Open: %.2f\n" +
                "Take Profit: %.2f\n" +
                "Stop Loss: %.2f\n" +
                "%s -> %s\n" +
                "-------------------------",
                open, takeProfit, stopLoss, openDate, closeDate);
    }


}
