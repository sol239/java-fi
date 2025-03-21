package com.github.sol239.javafi.instrument.java;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;

public interface JavaInstrument {

    String[] getColumnNames();

    String getName();

    String getDescription();

    double updateRow(HashMap<String, List<Double>> prices, Double... params);

}
