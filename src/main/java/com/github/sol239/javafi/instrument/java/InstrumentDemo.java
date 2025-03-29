package com.github.sol239.javafi.instrument.java;

import com.github.sol239.javafi.postgre.DBHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.github.sol239.javafi.instrument.java.InstrumentExecutor.runInstruments;

/**
 * This class demonstrates how to run instruments on a table.
 */
public class InstrumentDemo {

    /**
     * This is a demo of how to:
     * - run instruments on a table
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        /*
        // 1)
        // How to get the number of instruments:
        InstrumentExecutor ie = new InstrumentExecutor();
        int instrumentCount = ie.getInstrumentCount();
        System.out.println(instrumentCount);
        // -------------------------------------------------
        System.out.println("-------------------------------------------------");

        // 2)
        // How to get the available instruments:
        JavaInstrument[] availableInstruments = ie.getAvailableInstruments();
        for (JavaInstrument instrument : availableInstruments) {
            System.out.println(instrument.getName());
        }
        // -------------------------------------------------
        System.out.println("-------------------------------------------------");

        // 3)
        // Get an instrument by name and print its description:
        String instrumentName = "rsi";
        JavaInstrument instrument = ie.getInstrumentByName(instrumentName);
        if (instrument != null) {
            System.out.println(instrument.getDescription());
        } else {
            System.out.println("Instrument not found");
        }
        // -------------------------------------------------
        System.out.println("-------------------------------------------------");
        */

        // 4)
        // Run instruments on a table/s:
        // -------------------------------------------------
        Map<String, Double[]> instruments = new LinkedHashMap<>();
        String[] tableNames = new String[]{"btc_d"};                        // Table name/s
        instruments.put("rsi", new Double[]{14.0});                         // RSI with a 14-period sliding window
        instruments.put("bbl", new Double[]{14.0, 2.0});         // Bollinger Bands with 14-period sliding window and 2 standard deviation multiplier
        instruments.put("bbu", new Double[]{14.0, 2.0});
        instruments.put("ema", new Double[]{14.0});                         // EMA with a 14-period sliding window
        instruments.put("sma", new Double[]{30.0});                         // SMA with a 14-period sliding window
        instruments.put("macd", new Double[]{26.0, 12.0, 26.0, 9.0});       // MACD with 12-period short EMA, 26-period long EMA, and 9-period signal EMA

        runInstruments(Arrays.asList(tableNames), instruments);
        // -------------------------------------------------
        System.out.println("-------------------------------------------------");

        /*
        // 5)
        // Are there any conflicting instrument names = two cannot have same return value of getName()
        boolean namesValid = InstrumentValidator.areInstrumentNamesUnique(availableInstruments);
        System.out.println("Instrument names valid = " + namesValid);
        if (!namesValid) {
            List<JavaInstrument> duplicates = InstrumentValidator.getInstrumentHavingDuplicateName(availableInstruments);
            for (JavaInstrument duplicate : duplicates) {
                System.out.println("Duplicate instrument name: " + duplicate.getName());
            }
        }
        // -------------------------------------------------
        System.out.println("-------------------------------------------------");
        */


    }
}
