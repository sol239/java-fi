package com.github.sol239.javafi.instrument.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrument validator.
 */
public class InstrumentValidator {

    public static List<JavaInstrument> getInstrumentHavingDuplicateName(JavaInstrument[] instrumentNames) {
        List<JavaInstrument> duplicates = new ArrayList<>();
        for (int i = 0; i < instrumentNames.length; i++) {
            for (int j = i + 1; j < instrumentNames.length; j++) {
                if (instrumentNames[i].getName().equals(instrumentNames[j].getName())) {
                    duplicates.add(instrumentNames[i]);
                    duplicates.add(instrumentNames[j]);
                }
            }
        }
        return duplicates;
    }

    public static boolean areInstrumentNamesUnique(JavaInstrument[] instrumentNames) {
        List<JavaInstrument> duplicates = getInstrumentHavingDuplicateName(instrumentNames);
        return duplicates.isEmpty();
    }


}
