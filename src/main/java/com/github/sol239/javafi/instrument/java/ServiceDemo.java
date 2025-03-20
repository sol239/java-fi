package com.github.sol239.javafi.instrument.java;

import com.github.sol239.javafi.instrument.java.instruments.Rsi;
import com.github.sol239.javafi.postgre.DBHandler;

import java.util.LinkedHashMap;
import java.util.List;

public class ServiceDemo {
    public static void main(String[] args) {
        // INSTRUMENT EXECUTION
        InstrumentExecutor instrumentExecutor = new InstrumentExecutor();
        List<Double> columnVals = instrumentExecutor.getColumnValues("rsi", "btc_d", 14.0);
        System.out.println(columnVals);

        // DB INSERTION
        DBHandler db = new DBHandler();
        LinkedHashMap<String, List<Double>> columns = new LinkedHashMap<>();
        columns.put("rsi", columnVals);
        db.insertColumns("btx", columns);


    }
}
