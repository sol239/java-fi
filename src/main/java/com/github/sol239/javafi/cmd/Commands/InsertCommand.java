package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.postgre.DBHandler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class InsertCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "insert";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "The command inserts a csv file into the database.";
    }

    /**
     * Method to get the parameters of the command.
     *
     * @return parameters of the command
     */
    @Override
    public String getParameters() {
        return "";
    }

    /**
     * Method to run the command.
     *
     * @param args  arguments
     * @param flags flags
     * @return result
     */
    @Override
    public DataObject run(List<String> args, List<String> flags) {

        String tableName = "";
        String csvFilePath = "";

        DBHandler db = new DBHandler();

        for (String flag : flags) {
            if (flag.equals("-h") || flag.equals("-help")) {
                return new DataObject(200, "server", this.getDescription());
            } else if (flag.startsWith("--tn=") || flag.startsWith("--table_name=")) {
                tableName = flag.split("=")[1];
            } else if (flag.startsWith("--p=") || flag.startsWith("--path=")) {
                csvFilePath = flag.split("=")[1];
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

            // Read CSV header
            String headerLine = br.readLine();
            if (headerLine == null) {
                System.out.println("FAIL - Empty CSV file.");
                return new DataObject(400, "server", "Empty CSV file.");
            }

            // Convert CSV headers to lowercase for flexibility
            String[] csvHeaders = headerLine.toLowerCase().split(",");

            // Expected database columns
            List<String> dbColumns = Arrays.asList("timestamp", "open", "high", "low", "close", "volume", "date");

            // Create table if not exists
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "timestamp BIGINT,"
                    + "open DOUBLE PRECISION,"
                    + "high DOUBLE PRECISION,"
                    + "low DOUBLE PRECISION,"
                    + "close DOUBLE PRECISION,"
                    + "volume DOUBLE PRECISION,"
                    + "date TIMESTAMP"
                    + ");";

            try (Statement stmt = db.conn.createStatement()) {
                stmt.execute(createTableSQL);
            }

            // Find matching columns and their positions
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < csvHeaders.length; i++) {
                if (dbColumns.contains(csvHeaders[i])) {
                    columnIndexMap.put(csvHeaders[i], i);
                }
            }

            // Build dynamic SQL query based on available columns
            List<String> matchedColumns = new ArrayList<>(columnIndexMap.keySet());
            if (matchedColumns.isEmpty()) {
                System.out.println("FAIL - No matching columns found.");
                return new DataObject(400, "server", "No matching columns found.");
            }

            String sql = "INSERT INTO " + tableName + " (" + String.join(",", matchedColumns) + ") VALUES (" +
                    String.join(",", Collections.nCopies(matchedColumns.size(), "?")) + ")";

            // Prepare SQL statement
            try (PreparedStatement pstmt = db.conn.prepareStatement(sql)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    int paramIndex = 1;

                    // Assign only matched columns
                    for (String col : matchedColumns) {
                        int csvIndex = columnIndexMap.get(col);
                        String value = csvIndex < values.length ? values[csvIndex] : null;

                        // Convert types correctly
                        if (value == null || value.isEmpty()) {
                            pstmt.setNull(paramIndex, Types.NULL);
                        } else if (col.equals("timestamp")) {
                            pstmt.setLong(paramIndex, Long.parseLong(value));
                        } else if (Arrays.asList("open", "high", "low", "close", "volume").contains(col)) {
                            pstmt.setDouble(paramIndex, Double.parseDouble(value));
                        } else if (col.equals("date")) {
                            pstmt.setTimestamp(paramIndex, Timestamp.valueOf(value));
                        }

                        paramIndex++;
                    }

                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            return new DataObject(200, "server", "Data inserted to " + tableName);

        } catch (SQLException | IOException e) {
            return new DataObject(400, "server", "Error inserting data to " + tableName);
        }

    }
}
