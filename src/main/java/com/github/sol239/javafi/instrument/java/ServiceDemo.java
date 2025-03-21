package com.github.sol239.javafi.instrument.java;

import com.github.sol239.javafi.instrument.java.instruments.IdValueRecord;
import com.github.sol239.javafi.postgre.DBHandler;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

public class ServiceDemo {

    public static String benchMarkInstrumentExecution(int count, String[] tables, String instrumentName, Double... params) {

        long instrumentExecutionTime = 0;
        long dbInsertionTime = 0;
        long totalRows = 0;

        for (int i = 0; i < count; i++) {
            for (String table : tables) {

                long t1 = System.currentTimeMillis();

                InstrumentExecutor instrumentExecutor = new InstrumentExecutor();
                List<Double> columnVals = instrumentExecutor.getColumnValues(instrumentName, table, params);
                totalRows += columnVals.size();

                long t2 = System.currentTimeMillis();

                DBHandler db = new DBHandler();
                LinkedHashMap<String, List<Double>> columns = new LinkedHashMap<>();
                columns.put(instrumentName, columnVals);
                // db.insertColumns(table, columns);

                long t3 = System.currentTimeMillis();

                instrumentExecutionTime += t2 - t1;
                dbInsertionTime += t3 - t2;
                System.out.println((i + 1) + "/" + count + " - Instrument Execution Time: " + ((double) (t2 - t1) / 1000.0) + "| DB Insertion Time: " + (double) (t3 - t2) / 1000.0);

            }
        }

        String result = """
                Total Rows: %d
                Instrument Execution Time: %f s | DB Insertion Time: %f s | Total Time: %f s
                Avarage Execution Time: %f s | Avarage DB Insertion Time: %f s | Avarage Total Time: %f s
                """.formatted(
                totalRows,
                ((double) instrumentExecutionTime) / 1000.0,
                ((double) dbInsertionTime) / 1000.0,
                ((double) (instrumentExecutionTime + dbInsertionTime)) / 1000.0,
                ((double) instrumentExecutionTime) / 1000.0 / count,
                ((double) dbInsertionTime) / 1000.0 / count,
                ((double) (instrumentExecutionTime + dbInsertionTime)) / 1000.0 / count);

        return result;

    }


    public static void main(String[] args) {


        long t1 = System.currentTimeMillis();
        long rows = 0;
        // -------------------------------------------------
        Map<String, Double[]> instruments = new LinkedHashMap<>();
        String[] tableNames = new String[]{"btx"};
        instruments.put("rsi", new Double[]{14.0});               // RSI with a 14-period sliding window
        instruments.put("bollingerBands", new Double[]{14.0, 2.0}); // Bollinger Bands with 14-period sliding window and 2 standard deviation multiplier
        instruments.put("ema", new Double[]{14.0});                // EMA with a 14-period sliding window
        instruments.put("sma", new Double[]{30.0});                // SMA with a 14-period sliding window
        instruments.put("macd", new Double[]{26.0, 12.0, 26.0, 9.0});    // MACD with 12-period short EMA, 26-period long EMA, and 9-period signal EMA
        // -------------------------------------------------


        // All needed columns
        Set<String> columnNames = new HashSet<>();
        columnNames.add("id");
        List<JavaInstrument> _instruments = new ArrayList<>();

        InstrumentExecutor instrumentExecutor = new InstrumentExecutor();
        for (Map.Entry<String, Double[]> entry : instruments.entrySet()) {
            String instrumentName = entry.getKey();
            Double[] params = entry.getValue();

            for (JavaInstrument instrument : instrumentExecutor.getAvailableInstruments()) {
                if (instrumentName.equals(instrument.getName())) {
                    columnNames.addAll(Arrays.asList(instrument.getColumnNames()));
                    _instruments.add(instrument);
                }
            }
        }

        System.out.println("Columns[" + columnNames.size() + "]: " + columnNames);
        System.out.println("Instruments[" + _instruments.size() + "]: " + _instruments.stream().map(JavaInstrument::getName).toList());

        String names = String.join(", ", columnNames);
        DBHandler dbHandler = new DBHandler();
        ResultSet rs = dbHandler.getResultSet("SELECT " + names + " FROM " + tableNames[0] + " ORDER BY id ASC");


        // instrument name (rsi) : InstrumentHelper ( Map : string column name, List<Double> values)
        LinkedHashMap<String, InstrumentHelper> columns = new LinkedHashMap<>();
        for (String instrumentName : instruments.keySet()) {
            columns.put(instrumentName, new InstrumentHelper(instruments.get(instrumentName)[0].intValue()));
        }

        // map column name : Double representing row
        HashMap<String, Double> row = new LinkedHashMap<>();

        HashMap<String, List<IdValueRecord>> results = new LinkedHashMap<>();

        long id = 0;
        try {
            while(rs.next()) {

                for (String columnName : columnNames) {
                    row.put(columnName, rs.getDouble(columnName));
                }

                // print the row
                //System.out.println("Row[" + id + "]: " + row);
                id += 1;

                for (JavaInstrument instrument : _instruments) {
                    String name = instrument.getName();
                    InstrumentHelper helper = columns.get(name);

                    for (String param : instrument.getColumnNames()) {
                        helper.add(param, row.get(param));
                    }
                    columns.put(name, helper);


                    double val = instrument.updateRow(columns.get(name).stash, instruments.get(name));
                    // System.out.println(id + ": " + name + " -> " + val);

                    results.putIfAbsent(name, new ArrayList<>());
                    results.get(name).add(new IdValueRecord(id, val));
                    rows += 1;

                }
                row.clear();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long t2 = System.currentTimeMillis();

        // print results columns
        results.forEach((name, records) -> {System.out.println(name);});


        // FAST DB UPDATE
        dbHandler.insertColumnsBatch(tableNames[0], results);

        long t3 = System.currentTimeMillis();

        System.out.println("INSTRUMENT EXECUTION TIME: " + Double.parseDouble(String.valueOf(t2 - t1)) / 1000 + " s");
        System.out.println("DB INSERTION TIME: " + Double.parseDouble(String.valueOf(t3 - t2)) / 1000 + " s");
        System.out.println("Total Rows: " + rows);





        /*

        // INSTRUMENT EXECUTION
        InstrumentExecutor instrumentExecutor = new InstrumentExecutor();
        List<Double> columnVals = instrumentExecutor.getColumnValues(instrumentName, tableName, 14.0);

        //System.out.println(columnVals);
        //System.out.println("-------------------------------------------");

        System.out.println(columnVals.size());
        long t2 = System.currentTimeMillis();

        System.out.println("INSTRUMENT EXECUTION FINISHED");

        // DB INSERTION
        DBHandler db = new DBHandler();

        //db.insertDataWithCopy(tableName, "rsi", columnVals);

        LinkedHashMap<String, List<Double>> columns = new LinkedHashMap<>();
        columns.put(instrumentName, columnVals);
        db.insertColumns(tableName, columns);

        long t3 = System.currentTimeMillis();

        System.out.println("DB INSERTION FINISHED");
        System.out.println("-------------------------------------------");
        System.out.println("INSTRUMENT EXECUTION TIME: " + Double.parseDouble(String.valueOf(t2 - t1)) / 1000 + " s");
        System.out.println("DB INSERTION TIME: " + Double.parseDouble(String.valueOf(t3 - t2)) / 1000 + " s"); */


    }
}
