package com.github.sol239.javafi.instrument.java;

import com.github.sol239.javafi.postgre.DBHandler;

import java.sql.ResultSet;
import java.util.*;

/**
 * This class executes instruments on a table.
 */
public class InstrumentExecutor {

    /**
     * The available instruments.
     */
    JavaInstrument[] availableInstruments;

    public InstrumentExecutor() {
        ServiceLoader<JavaInstrument> loader = ServiceLoader.load(JavaInstrument.class);

        // loading all available instruments using ServiceLoader
        this.availableInstruments = loader.stream().map(ServiceLoader.Provider::get).toArray(JavaInstrument[]::new);
    }

    /**
     * Get the number of available instruments.
     * @return the number of available instruments
     */
    public int getInstrumentCount() {
        return availableInstruments.length;
    }

    /**
     * Get the available instruments.
     * @return the available instruments
     */
    public JavaInstrument[] getAvailableInstruments() {
        return availableInstruments;
    }

    /**
     * Get an instrument by name.
     * @param name the name of the instrument
     * @return the instrument
     */
    public JavaInstrument getInstrumentByName(String name) {
        for (JavaInstrument instrument : this.getAvailableInstruments()) {
            if (name.equals(instrument.getName())) {
                return instrument;
            }
        }
        return null;
    }

    /**
     * Run an instrument on a table - this method is deprecated. It used different approach to run instruments.
     * @param instrumentName the name of the instrument
     * @param tableName the name of the table
     * @param params the parameters for the instrument
     * @return the values of the column
     */
    @Deprecated
    public List<Double> getColumnValues(String instrumentName, String tableName, Double... params) {

        // GENERAL
        int stashSize = params[0].intValue();
        List<Double[]> stash = new ArrayList<>();

        // GENERAL
        DBHandler dbHandler = new DBHandler();

        // GENERAL
        ResultSet rs = dbHandler.getResultSet("SELECT * FROM " + tableName + " ORDER BY id ASC");

        // GENERAL
        // Chceme naplnit List columnValues:

        JavaInstrument instrument = null;
        Double[] values;
        List<Double> columnValues = new ArrayList<>();


        for (JavaInstrument _instrument : availableInstruments) {
            if (instrumentName.equals(_instrument.getName())) {
                instrument = _instrument;
            } else {
                return null;
            }}

        values = new Double[instrument.getColumnNames().length];

        // iterate over db
        int id = 0;
        int x = 0;
        try {
            while (rs.next()) {
                id += 1;
                for (int i = 0; i < instrument.getColumnNames().length; i++) {
                    values[i] = rs.getDouble(instrument.getColumnNames()[i]);
                }

                stash.add(values.clone());

                if (stash.size() == stashSize) {
                    stash.remove(0);
                    continue;
                }

                x++;

            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return columnValues;
    }
}
