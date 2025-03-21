package com.github.sol239.javafi.instrument.java;

import com.github.sol239.javafi.postgre.DBHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

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
                db.insertColumns(table, columns);

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


    public static void main(String[] args) throws SQLException, IOException {

        String result = benchMarkInstrumentExecution(5, new String[] {"btx"}, "rsi", 14.0);
        System.out.println(result);


        // timer
        /*
        long t1 = System.currentTimeMillis();

        String tableName = "btx";
        String instrumentName = "rsi";

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
        System.out.println("DB INSERTION TIME: " + Double.parseDouble(String.valueOf(t3 - t2)) / 1000 + " s");
        */

    }
}
