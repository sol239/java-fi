package com.github.sol239.javafi.utils.instrument.instruments;

import com.github.sol239.javafi.utils.instrument.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of Simple Moving Average (SMA) indicator.
 * @see <a href="https://www.investopedia.com/terms/s/sma.asp">Investopedia</a>
 */
@AutoService(JavaInstrument.class)
public class SMA implements JavaInstrument {

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
        return "sma";
    }

    /**
     * Get the description of the instrument.
     * @return the description of the instrument
     */
    @Override
    public String getDescription() {
        return "The Simple Moving Average (SMA) is the unweighted mean of the previous n data points.";
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
        double sum = 0;

        for (int i = closes.size() - period; i < closes.size(); i++) {
            sum += closes.get(i);
        }

        return sum / period;
    }
}
