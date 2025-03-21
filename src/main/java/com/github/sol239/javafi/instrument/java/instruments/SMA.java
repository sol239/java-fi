package com.github.sol239.javafi.instrument.java.instruments;

import com.github.sol239.javafi.instrument.java.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.List;

@AutoService(JavaInstrument.class)
public class SMA implements JavaInstrument {

    @Override
    public String[] getColumnNames() {
        return new String[]{"close"};  // Only "close" is used in the calculation
    }

    @Override
    public String getName() {
        return "sma";
    }

    @Override
    public String getDescription() {
        return "The Simple Moving Average (SMA) is the unweighted mean of the previous n data points.";
    }

    @Override
    public double updateRow(HashMap<String, List<Double>> prices, Double... params) {
        int period = params[0].intValue(); // Sliding window size

        if (prices == null || prices.get("close").size() < period) {
            return 0;
        }

        List<Double> closes = prices.get("close");
        double sum = 0;

        for (int i = closes.size() - period; i < closes.size(); i++) {
            sum += closes.get(i);
        }

        return sum / period;
    }
}
