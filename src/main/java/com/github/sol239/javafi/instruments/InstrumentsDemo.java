package com.github.sol239.javafi.instruments;

import com.github.sol239.javafi.instruments.java.Rsi;
import com.github.sol239.javafi.postgre.DBHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class InstrumentsDemo {
    public static void main(String[] args) {
        Rsi rsi = new Rsi();
        List<Double> vals = rsi.getColumnValues("btc_d", 15.0);
        DBHandler db = new DBHandler();

        System.out.println(vals);

        LinkedHashMap<String, List<Double>> columns = new LinkedHashMap<>();
        columns.put("rsi", vals);

        db.insertColumns("btc_d", columns);
    }
}
