package com.github.sol239.javafi.instruments.java;


import com.github.sol239.javafi.postgre.DBHandler;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Rsi {

    public static double calculateRSI(List<Double> prices, int period) {
        if (prices == null || prices.size() < period + 1) {
            throw new IllegalArgumentException("Not enough data points to calculate RSI");
        }

        double gainSum = 0, lossSum = 0;

        for (int i = 1; i <= period; i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gainSum += change;
            } else {
                lossSum += Math.abs(change);
            }
        }

        double avgGain = gainSum / period;
        double avgLoss = lossSum / period;

        for (int i = period + 1; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            double gain = change > 0 ? change : 0;
            double loss = change < 0 ? Math.abs(change) : 0;

            avgGain = ((avgGain * (period - 1)) + gain) / period;
            avgLoss = ((avgLoss * (period - 1)) + loss) / period;
        }

        double rs = avgLoss == 0 ? Double.POSITIVE_INFINITY : avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    public List<Double> getColumnValues(String tableName, Double... params) {
        DBHandler dbHandler = new DBHandler();
        ResultSet rs = dbHandler.getResultSet("SELECT * FROM " + tableName);
        List<Double> columnValues = new ArrayList<>();

        int rsiPeriod = params[0].intValue();
        Double[] rsiValues = new Double[rsiPeriod];

        int i = 0;
        try {
            while (rs.next()) {
                double close = rs.getDouble("close");
                rsiValues[i] = close;
                i++;
                if (i == rsiPeriod) {
                    i = 0;
                }
                try {
                    columnValues.add(calculateRSI(List.of(rsiValues), rsiPeriod - 1));
                } catch (Exception e) {
                    columnValues.add(0.0);
                }



            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }


        return columnValues;
    }
}
