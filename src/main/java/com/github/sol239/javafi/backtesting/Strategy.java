package com.github.sol239.javafi.backtesting;

public class Strategy {
    public String name;
    public String description;
    public String personalNote;

    double initialBalance;
    double balance;

    double profit;
    double loss;
    double riskRewardRatio;

    String sqlWhereClause;
}
