package com.github.sol239.javafi.instrument.java.instruments;

import com.github.sol239.javafi.instrument.java.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.List;

@AutoService(JavaInstrument.class)
public class EMA implements JavaInstrument {

    @Override
    public String[] getColumnNames() {
        return new String[]{"close"};  // Only "close" is used in the calculation
    }

    @Override
    public String getName() {
        return "ema";
    }

    @Override
    public String getDescription() {
        return "The Exponential Moving Average (EMA) gives more weight to recent prices, making it more responsive to new information.";
    }

    @Override
    public double updateRow(HashMap<String, List<Double>> prices, Double... params) {
        int period = params[0].intValue(); // Sliding window size

        if (prices == null || prices.get("close").size() < period) {
            return 0;
        }

        List<Double> closes = prices.get("close");
        double multiplier = 2.0 / (period + 1);
        double ema = closes.get(closes.size() - period); // Initialize with the first value in the window

        for (int i = closes.size() - period + 1; i < closes.size(); i++) {
            ema = ((closes.get(i) - ema) * multiplier) + ema;
        }
        return ema;
    }
}
