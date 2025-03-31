package com.github.sol239.javafi.utils.instrument.java;

import java.util.HashMap;
import java.util.List;

/**
 * The interface for Java instruments.
 */
public interface JavaInstrument {

    /**
     * Get the column names needed to run the instrument = columns which will be fetched from the table.
     * @return the column names
     */
    String[] getColumnNames();

    /**
     * Get the name of the instrument.
     * So the instrument can be identified by its given name and not by the class name.
     * @return the name of the instrument
     */
    String getName();

    /**
     * Get the description of the instrument.
     * @return the description of the instrument
     */
    String getDescription();

    /**
     * Calculates the value which be inserted into the column
     * @param prices the prices of the instrument - key is the column name, value is the list of prices - values
     * @param params the parameters for the instrument
     * @return the value to be inserted into the column
     */
    double updateRow(HashMap<String, List<Double>> prices, Double... params);

}
