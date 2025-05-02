package com.github.sol239.javafi.utils.instrument.instruments;

import com.github.sol239.javafi.utils.instrument.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of Exponential Moving Average (EMA) indicator.
 * @see <a href="https://www.investopedia.com/terms/e/ema.asp">Investopedia</a>
 */
@AutoService(JavaInstrument.class)
public class EMA implements JavaInstrument {

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
        return "ema";
    }

    /**
     * Get the description of the instrument.
     * @return the description of the instrument
     */
    @Override
    public String getDescription() {
        return "The Exponential Moving Average (EMA) gives more weight to recent prices, making it more responsive to new information." + "\n" +
                "Usage: ema:period\n";
    }

    /**
     * Calculates the value which be inserted into the column
     * @param prices the prices of the instrument - key is the column name, value is the list of prices - values
     * @param params the parameters for the instrument
     * @return the value to be inserted into the column
     */
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
