package com.github.sol239.javafi.instrument.java.instruments;


import com.github.sol239.javafi.instrument.java.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.List;
import java.util.Queue;

@AutoService(JavaInstrument.class)
public class Rsi implements JavaInstrument {


    @Override
    public String[] getColumnNames() {
        return new String[]{"close"};
    }

    @Override
    public String getName() {
        return "rsi";
    }

    @Override
    public String getDescription() {
        return "The relative strength index (RSI) is a momentum indicator that measures the magnitude of recent price changes to evaluate overbought or oversold conditions in the price of a stock or other asset.";
    }

    @Override
    public double updateRow(List<Double[]> prices, Double... params) {
        int period = params[0].intValue();

        if (prices == null || prices.size() < period) {
            return 0;
        }

        double gainSum = 0, lossSum = 0;

        for (int i = 1; i < period; i++) {
            double change = prices.get(i)[0] - prices.get(i - 1)[0];
            if (change > 0) {
                gainSum += change;
            } else {
                lossSum += Math.abs(change);
            }
        }

        double avgGain = gainSum / period;
        double avgLoss = lossSum / period;

        for (int i = period + 1; i < prices.size(); i++) {
            double change = prices.get(i)[0] - prices.get(i - 1)[0];
            double gain = change > 0 ? change : 0;
            double loss = change < 0 ? Math.abs(change) : 0;

            avgGain = ((avgGain * (period - 1)) + gain) / period;
            avgLoss = ((avgLoss * (period - 1)) + loss) / period;
        }

        double rs = avgLoss == 0 ? Double.POSITIVE_INFINITY : avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }
}
