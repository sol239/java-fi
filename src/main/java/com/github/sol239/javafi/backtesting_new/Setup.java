package com.github.sol239.javafi.backtesting_new;

public class Setup {
    public double balance;
    public double leverage;
    public double fee;
    public double takeProfit;
    public double stopLoss;
    public double amount;
    public double riskReward;
    public int maxTrades;
    public int delaySeconds;

    /**
     * Stores the maximum number of opened trades at the same time.
     */
    public int maxOpenedTrades;

    public Setup(double balance, double leverage, double fee, double takeProfit, double stopLoss, double amount, double riskReward, int maxTrades, int delaySeconds) {
        this.balance = balance;
        this.leverage = leverage;
        this.fee = fee;
        this.takeProfit = takeProfit;
        this.stopLoss = stopLoss;
        this.amount = amount;
        this.riskReward = riskReward;
        this.maxTrades = maxTrades;
        this.delaySeconds = delaySeconds;
        this.maxOpenedTrades = 0;
    }

    @Override
    public String toString() {
        return "balance = " + balance + "\n" +
                "leverage = " + leverage + "\n" +
                "fee = " + fee + "\n" +
                "takeProfit = " + takeProfit + "\n" +
                "stopLoss = " + stopLoss + "\n" +
                "amount = " + amount + "\n" +
                "riskReward = " + riskReward + "\n" +
                "maxTrades = " + maxTrades + "\n" +
                "delaySeconds = " + delaySeconds + "\n" +
                "maxOpenedTrades = " + maxOpenedTrades;
    }
}
