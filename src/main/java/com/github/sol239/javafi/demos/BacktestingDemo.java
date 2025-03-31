package com.github.sol239.javafi.demos;

import com.github.sol239.javafi.utils.backtesting.BacktestingExecutor;
import com.github.sol239.javafi.utils.backtesting.Setup;

public class BacktestingDemo {
    public static void main(String[] args) {
        String originalOpenClause = "WHERE rsi_14_ins_ <= 27";
        String originalCloseClause = "WHERE rsi_14_ins_ >= 70";
        String tableName = "solx";

        // ---------------- Setup parameters ----------------
        double balance = 10000;
        double leverage = 5;
        double fee = (double) 2.5 / 1000;
        double takeProfit = 0.1;
        double stopLoss = 0.4;
        double amount = 500;
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
        // TODO: automatic
        backtestingExecutor.updateStrategyColumns(tableName);

        System.out.println("****************************************");
        System.out.println(backtestingExecutor.setup);
        System.out.println("****************************************\n");


        backtestingExecutor.run(tableName, setup);
    }
}
