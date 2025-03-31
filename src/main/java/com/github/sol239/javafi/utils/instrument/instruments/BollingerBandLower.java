package com.github.sol239.javafi.utils.instrument.instruments;

import com.github.sol239.javafi.utils.instrument.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of Bollinger Bands indicator.
 * @see <a href="https://www.investopedia.com/terms/b/bollingerbands.asp">Investopedia</a>
 */
@AutoService(JavaInstrument.class)
public class BollingerBandLower implements JavaInstrument {

    @Override
    public String[] getColumnNames() {
        return new String[]{"close"};  // Only "close" is used in the calculation
    }

    @Override
    public String getName() {
        return "bbl";
    }


    @Override
    public String getDescription() {
        return "Bollinger Bands consist of a middle band (SMA) and two outer bands that are two standard deviations away from the middle band.";
    }

    @Override
    public double updateRow(HashMap<String, List<Double>> prices, Double... params) {
        int period = params[0].intValue();
        double multiplier = (params.length > 1) ? params[1] : 2;

        if (prices == null || prices.get("close").size() < period) {
            return 0;
        }

        List<Double> closes = prices.get("close");
        double sum = 0;

        for (int i = closes.size() - period; i < closes.size(); i++) {
            sum += closes.get(i);
        }

        double sma = sum / period;
        double squaredDifferences = 0;

        for (int i = closes.size() - period; i < closes.size(); i++) {
            squaredDifferences += Math.pow(closes.get(i) - sma, 2);
        }

        double variance = squaredDifferences / period;
        double standardDeviation = Math.sqrt(variance);

        double upperBand = sma + multiplier * standardDeviation;
        double lowerBand = sma - multiplier * standardDeviation;

        // Returning upper band as the result
        return lowerBand;
    }
}
