package com.github.sol239.javafi.utils.instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Helper class for handling calculations of instrument data.
 */
public class InstrumentHelper {

    /**
     * The size of the stash.
     */
    public int stashSize;

    /**
     * The stash for storing data.
     */
    public HashMap<String, List<Double>> stash = new HashMap<>();

    /**
     * Constructor for the InstrumentHelper class.
     * @param stashSize the size of the stash
     */
    public InstrumentHelper(int stashSize) {
        this.stashSize = stashSize;
    }

    /**
     * Adds a value to the stash.
     * @param column the column name
     * @param value the value to add
     */
    public void add(String column, double value) {
        stash.putIfAbsent(column, new ArrayList<>());
        if (stash.get(column).size() < stashSize) {
            stash.get(column).add(value);
        } else {
            // remove first added element
            stash.get(column).removeFirst();
            stash.get(column).add(value);
        }
    }

    /**
     * Clears the stash.
     */
    public void clear() {
        stash.clear();
    }

    /**
     * Returns the length of the stash.
     * @return the length of the stash
     */
    public int length() {
        // get length of the first column;
        return stash.values().stream().findFirst().map(List::size).orElse(0);
    }

    /**
     * Returns the string representation of the stash.
     * @return the string representation of the stash
     */
    @Override
    public String toString() {
        return stash.toString();
    }
}
