package com.github.sol239.javafi.demos;

import com.github.sol239.javafi.utils.backtesting.BacktestingExecutor;
import com.github.sol239.javafi.utils.backtesting.Setup;
import com.github.sol239.javafi.utils.backtesting.Strategy;

import java.util.Scanner;

public class BacktestingDemo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String originalOpenClause = "WHERE rsi_14_ins_ <= 30";
        String originalCloseClause = "WHERE rsi_14_ins_ >= 80";
        String tableName = "solx";

        // ---------------- Setup parameters ----------------
        double balance = 10000;
        double leverage = 12;
        double fee = (double) 2.5 / 1000;
        double takeProfit = 1.01;
        double stopLoss = 0.99;
        double amount = 500;
        double riskReward = (takeProfit) / (stopLoss);
        int maxTrades = 5;
        int delaySeconds = 3600 * 3;
        Setup longSetup = new Setup(balance, leverage, fee, takeProfit, stopLoss, amount, riskReward, maxTrades, delaySeconds);
        Setup shortSetup = new Setup(balance, leverage, fee, 0.98, 1.01, amount, riskReward, maxTrades, delaySeconds);

        // -------------------------------------------------

        Strategy long_1 = new Strategy(originalOpenClause, originalCloseClause, longSetup);
        Strategy short_1 = new Strategy(originalCloseClause, originalOpenClause, shortSetup);

        // TODO: Add long trade strategy
        //  Add short trade strategy (longs.add(), shorts.add())
        //  List of longs and shorts strategies to be executed.
        BacktestingExecutor backtestingExecutor = new BacktestingExecutor();

        backtestingExecutor.addStrategy(long_1);
        // backtestingExecutor.addStrategy(short_1);

        // backtestingExecutor.addLongStrategy(setup, openClause, closeClause);
        // backtestingExecutor.addShortStrategy(setup, openClause, closeClause);

        backtestingExecutor.createStrategiesColumns(tableName);


        // backtestingExecutor.updateStrategiesColumns(tableName);   // TODO: can be made automatic.

        System.out.println("****************************************");


        backtestingExecutor.run(tableName, 0, true, true);
    }
}
