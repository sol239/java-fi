package com.github.sol239.javafi.backtesting;

public class Strategy {
    public String name;

    public double initialBalance;
    public double balance;

    double assetBalance;

    public double profit;
    public double loss;
    double riskRewardRatio;


    public Strategy(String name, double initialBalance, double profit, double loss) {
        this.name = name;
        this.initialBalance = initialBalance;
        this.balance = initialBalance;

        this.profit = profit;
        this.loss = loss;

        this.assetBalance = 0;
    }






}
