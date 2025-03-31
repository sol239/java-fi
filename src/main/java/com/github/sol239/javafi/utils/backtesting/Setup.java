package com.github.sol239.javafi.utils.backtesting;

/**
 * Stores the setup for the trading/backtesting.
 */
public class Setup {
    /**
     * Current balance.
     */
    public double balance;

    /**
     * Leverage used for trading. Default is 1.
     */
    public double leverage;

    /**
     * Fee for each trade. e.g. Binance has 0.1% maker fee and 0.1% taker fee. => fee = 0.001 + 0.001 = 0.002
     */
    public double fee;

    public double takeProfit;
    public double stopLoss;

    /**
     * Amount of money to be used for each trade.
     */
    public double amount;

    /**
     * Risk reward ratio.
     */
    public double riskReward;

    /**
     * Maximum number of trades to be opened at the same time.
     */
    public int maxTrades;

    /**
     * Delay in seconds between each trade.
     */
    public int delaySeconds;

    public String dateRestriction;

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
