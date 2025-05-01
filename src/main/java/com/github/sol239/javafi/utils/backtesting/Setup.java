package com.github.sol239.javafi.utils.backtesting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

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

    /**
     * Date restriction for the trades. e.g. "2023-01-01 00:00:00" or "2023-01-01 00:00:00 - 2023-01-02 00:00:00"
     */
    public String dateRestriction;

    /**
     * Stores the maximum number of opened trades at the same time.
     */
    public int maxOpenedTrades;

    /**
     * The maximum number of seconds a trade can be open.
     * Default is 3600 * 24 = 1 day.
     */
    public int tradeLifeSpanSeconds;

    public Setup(double balance, double leverage, double fee, double takeProfit, double stopLoss, double amount, double riskReward, int maxTrades, int delaySeconds, String dateRestriction, int tradeLifeSpanSeconds) {
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
        this.dateRestriction = dateRestriction;
        this.tradeLifeSpanSeconds = tradeLifeSpanSeconds;
    }

    public Setup() {

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
                "maxOpenedTrades = " + maxOpenedTrades + "\n" +
                "dateRestriction = " + dateRestriction + "\n" +
                "tradeLifeSpanSeconds = " + tradeLifeSpanSeconds + "\n";
    }


    public static Setup fromJson(String jsonPath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(new File(jsonPath));
            double balance = node.get("balance").asDouble();
            double leverage = node.get("leverage").asDouble();
            double fee = node.get("fee").asDouble();
            double takeProfit = node.get("takeProfit").asDouble();
            double stopLoss = node.get("stopLoss").asDouble();
            double amount = node.get("amount").asDouble();
            double riskReward = node.has("riskReward") ? node.get("riskReward").asDouble() : takeProfit / stopLoss;
            int maxTrades = node.get("maxTrades").asInt();
            int delaySeconds = node.get("delaySeconds").asInt();
            String dateRestriction = node.get("dateRestriction").asText();
            int tradeLifeSpanSeconds = node.has("tradeLifeSpanSeconds") ? node.get("tradeLifeSpanSeconds").asInt() : 3600 * 24;

            return new Setup(balance, leverage, fee, takeProfit, stopLoss, amount, riskReward, maxTrades, delaySeconds, dateRestriction, tradeLifeSpanSeconds);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
