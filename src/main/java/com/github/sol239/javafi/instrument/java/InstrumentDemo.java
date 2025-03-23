package com.github.sol239.javafi.instrument.java;

import com.github.sol239.javafi.postgre.DBHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * This class demonstrates how to run instruments on a table.
 */
public class InstrumentDemo {

    /**
     * Run instruments on a list of tables.
     * @param tableNames the names of the tables
     * @param instruments the instruments to run
     */
    public static void runInstruments(List<String> tableNames, Map<String, Double[]> instruments ) {
        for (String tableName : tableNames) {
            runInstrument(tableName, instruments);
        }
    }

    public static String getInstrumentDBColumnName(Map<String, Double[]> instruments, String instrumentName) {
        StringBuilder params = new StringBuilder();
        for (Double param : instruments.get(instrumentName)) {
            params.append(param.intValue()).append("_");
        }
        params.delete(params.length() - 1, params.length());
        return instrumentName + "_" + params.toString() + "_ins_";
    }

    /**
     * Run instruments on a table.
     * @param tableName the name of the table
     * @param instruments the instruments to run
     */
    public static void runInstrument(String tableName, Map<String, Double[]> instruments) {

        Map<String, Double[]> instrumentsRemapped = new LinkedHashMap<>();
        for (Map.Entry<String, Double[]> entry : instruments.entrySet()) {
            String key = entry.getKey();
            Double[] value = entry.getValue();
            key = getInstrumentDBColumnName(instruments, key);
            instrumentsRemapped.put(key, value);
        }

        long t1 = System.currentTimeMillis();
        long rows = 0;

        // All needed columns
        Set<String> columnNames = new HashSet<>();
        columnNames.add("id");
        List<JavaInstrument> _instruments = new ArrayList<>();

        InstrumentExecutor instrumentExecutor = new InstrumentExecutor();
        for (Map.Entry<String, Double[]> entry : instruments.entrySet()) {
            Double[] params = entry.getValue();
            String instrumentName = entry.getKey();


            // TODO - je potřeba vyřešit ukládání názvů sloupců podle parametrl isntrumentu - každá instance instrumentu musí si vytvářet název DB sloupce podle parametrů

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
        ResultSet rs = dbHandler.getResultSet("SELECT " + names + " FROM " + tableName + " ORDER BY id ASC");


        // instrument name (rsi) : InstrumentHelper ( Map : string column name, List<Double> values)
        LinkedHashMap<String, InstrumentHelper> columns = new LinkedHashMap<>();
        for (String instrumentName : instruments.keySet()) {
            String name = getInstrumentDBColumnName(instruments, instrumentName);
            columns.put(name, new InstrumentHelper(instruments.get(instrumentName)[0].intValue()));
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

                id += 1;

                for (JavaInstrument instrument : _instruments) {
                    String name = instrument.getName();

                    name = getInstrumentDBColumnName(instruments, name);

                    InstrumentHelper helper = columns.get(name);

                    for (String param : instrument.getColumnNames()) {
                        helper.add(param, row.get(param));
                    }
                    columns.put(name, helper);

                    System.out.println(name + " : " + row.get(name));

                    //double val = 1.0;
                    double val = instrument.updateRow(columns.get(name).stash, instrumentsRemapped.get(name));
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
        dbHandler.insertColumnsBatch(tableName, results);

        long t3 = System.currentTimeMillis();

        System.out.println("INSTRUMENT EXECUTION TIME: " + Double.parseDouble(String.valueOf(t2 - t1)) / 1000 + " s");
        System.out.println("DB INSERTION TIME: " + Double.parseDouble(String.valueOf(t3 - t2)) / 1000 + " s");
        System.out.println("Total Rows: " + rows);
    }

    /**
     * This is a demo of how to:
     * - run instruments on a table
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
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



    }
}
