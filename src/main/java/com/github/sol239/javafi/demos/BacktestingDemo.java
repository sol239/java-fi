package com.github.sol239.javafi.demos;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.backtesting.BacktestingExecutor;
import com.github.sol239.javafi.utils.backtesting.Setup;
import com.github.sol239.javafi.utils.backtesting.Strategy;

public class BacktestingDemo {
    public static void main(String[] args) {

        // ---------------- Setup parameters ----------------
        /*
        double balance = 10000;
        double leverage = 12;
        double fee = 2.5 / 1000.0;
        double takeProfit = 1.01;
        double stopLoss = 0.99;
        double amount = 500;
        int maxTrades = 5;
        int delaySeconds = 3600 * 3;
        int tradeLifespanSeconds = 3600 * 24;
        Setup longSetup = new Setup(balance, leverage, fee, takeProfit, stopLoss, amount, (takeProfit) / (stopLoss), maxTrades, delaySeconds);
        // --------------------------------------------------
        */

        String tableName = "btc";
        String setupPath = "assets/setups/setup_1.json";
        String resultJsonPath = "assets/results/trades.json";
        String strategyPath = "assets/strategies/rsi_strategy.json";

        Setup longSetup = Setup.fromJson(setupPath);
        Strategy strategy = new Strategy(longSetup);
        strategy.loadClausesFromJson(strategyPath);

        BacktestingExecutor backtestingExecutor = new BacktestingExecutor();
        backtestingExecutor.addStrategy(strategy);
        backtestingExecutor.createStrategiesColumns(tableName);

        System.out.println(strategy);

        backtestingExecutor.updateStrategiesColumns(tableName);   // TODO: does not have to be executed each time
        DataObject result = backtestingExecutor.run(tableName, longSetup.tradeLifeSpanSeconds, false, true, resultJsonPath, longSetup.dateRestriction);

        System.out.println(result);
    }
}
