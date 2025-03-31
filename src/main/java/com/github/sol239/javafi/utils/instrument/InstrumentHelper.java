package com.github.sol239.javafi.utils.instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InstrumentHelper {


    public int stashSize;

    public InstrumentHelper(int stashSize) {
        this.stashSize = stashSize;
    }

    public HashMap<String, List<Double>> stash = new HashMap<>();

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

    public void clear() {
        stash.clear();
    }

    public int length() {
        // get length of the first column;
        return stash.values().stream().findFirst().map(List::size).orElse(0);
    }


    @Override
    public String toString() {
        return stash.toString();
    }
}
