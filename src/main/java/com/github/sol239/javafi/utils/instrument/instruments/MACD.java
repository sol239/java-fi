package com.github.sol239.javafi.utils.instrument.instruments;

import com.github.sol239.javafi.utils.instrument.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of Moving Average Convergence Divergence (MACD) indicator.
 * @see <a href="https://www.investopedia.com/terms/m/macd.asp">Investopedia</a>
 */
@AutoService(JavaInstrument.class)
public class MACD implements JavaInstrument {

    /**
     * Get the column names needed to run the instrument = columns which will be fetched from the table.
     * @return the column names
     */
    @Override
    public String[] getColumnNames() {
        return new String[]{"close"};  // Only "close" is used in the calculation
    }

    /**
     * Get the name of the instrument.
     * So the instrument can be identified by its given name and not by the class name.
     * @return the name of the instrument
     */
    @Override
    public String getName() {
        return "macd";
    }

    /**
     * Get the description of the instrument.
     * @return the description of the instrument
     */
    @Override
    public String getDescription() {
        return "The Moving Average Convergence Divergence (MACD) is a trend-following momentum indicator that shows the relationship between two moving averages of a security's price." + "\n" +
                "Usage: macd:slidingWindow,shortPeriod,longPeriod,signalPeriod\n";
    }

    /**
     * Calculates the value which be inserted into the column
     * @param prices the prices of the instrument - key is the column name, value is the list of prices - values
     * @param params the parameters for the instrument
     * @return the value to be inserted into the column
     */
    @Override
    public double updateRow(HashMap<String, List<Double>> prices, Double... params) {
        int slidingWindow = params[0].intValue(); // Sliding window size for EMA
        int shortPeriod = params[1].intValue();  // Short period for EMA
        int longPeriod = params[2].intValue();   // Long period for EMA
        int signalPeriod = params[3].intValue(); // Signal line EMA period

        if (prices == null || prices.get("close").size() < longPeriod) {
            return 0;
        }

        List<Double> closes = prices.get("close");

        // Calculate the short-term and long-term EMAs
        double shortEma = calculateEMA(closes, shortPeriod);
        double longEma = calculateEMA(closes, longPeriod);

        // MACD Line = Short EMA - Long EMA
        double macdLine = shortEma - longEma;

        // Calculate the signal line (EMA of the MACD line)
        List<Double> macdList = List.of(macdLine);  // Just one MACD value for now
        double signalLine = calculateEMA(macdList, signalPeriod);

        // Return the MACD Histogram: MACD Line - Signal Line
        // TODO: macd usually consists of 2 lines, but we are only returning the histogram
        /*
        What Each Value Represents:
        - MACD Line: macdLine = shortEma - longEma (The difference between the short and long EMAs).
        - Signal Line: signalLine = calculateEMA(macdList, signalPeriod) (The EMA of the MACD line, typically with a 9-period).
        - MACD Histogram: macdHistogram = macdLine - signalLine (The difference between the MACD line and the signal line, typically shown as bars).
         */
        return macdLine - signalLine;
    }

    private double calculateEMA(List<Double> prices, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = 0;

        // If there are enough data points, initialize the EMA
        if (prices.size() >= period) {
            // Start by calculating the simple moving average (SMA) for the first 'period' elements
            double sum = 0;
            for (int i = 0; i < period; i++) {
                sum += prices.get(i);
            }
            ema = sum / period;  // Initial SMA value
        }

        // Apply the multiplier for the rest of the prices
        for (int i = period; i < prices.size(); i++) {
            ema = ((prices.get(i) - ema) * multiplier) + ema;
        }

        return ema;
    }
}
