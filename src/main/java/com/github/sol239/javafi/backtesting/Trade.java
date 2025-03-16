package com.github.sol239.javafi.backtesting;

import java.sql.Date;

public class Trade {

    private Class<?> strategy;
    private Date openDate;
    private Date closeDate;
    private double openPrice;
    private double closePrice;
    private double amount;

    public Trade(Class<?> strategy, Date openDate, Date closeDate, double openPrice, double closePrice, double amount) {
        this.strategy = strategy;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.amount = amount;
    }




}
