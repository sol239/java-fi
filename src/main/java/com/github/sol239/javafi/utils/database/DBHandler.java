package com.github.sol239.javafi.utils.database;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.instrument.IdValueRecord;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static java.sql.DriverManager.getConnection;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * DBHandler is a utility class for managing database connections and operations.
 */
public class DBHandler {

    /**
     * The connection to the H2 database.
     */
    public Connection conn;

    /**
     * Default constructor that initializes the database connection.
     */
    public DBHandler() {
        connect();
    }

    /**
     * H2 system tabeĺes to exclude from the list of tables.
     */
    public static final List<String> SYSTEM_TABLES = Arrays.asList(
            "CONSTANTS",
            "ENUM_VALUES",
            "INDEXES",
            "INDEX_COLUMNS",
            "INFORMATION_SCHEMA_CATALOG_NAME",
            "IN_DOUBT",
            "LOCKS",
            "QUERY_STATISTICS",
            "RIGHTS",
            "ROLES",
            "SESSIONS",
            "SESSION_STATE",
            "SETTINGS",
            "SYNONYMS",
            "USERS"
    );

    /**
     * Establishes a connection to the H2 database using the provided URL.
     */
    public void connect() {
        Dotenv dotenv = Dotenv.load();
        String DB_URL = dotenv.get("DB_URL");
        this.conn = null;
        try {
            this.conn = getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    /**
     * Checks if the connection to the database is established.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return this.conn != null;
    }

    /**
     * Retrieves all tables from the database, excluding H2 system tables.
     * @return a list of table names
     */
    public List<String> getAllTables() {

        try {
            DatabaseMetaData dbmd = this.conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, "%", new String[]{"TABLE"});
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                String tableName = rs.getString(3);
                if (!SYSTEM_TABLES.contains(tableName)) {
                    tables.add(tableName);
                }
            }
            return tables;
        } catch (SQLException e) {
            System.out.println("Error getting tables: " + e.getMessage());
            return null;
        }
    }

    /**
     * Cleans the specified table by removing all columns except for the specified ones.
     * @param tableName the name of the table to clean
     */
    public void clean(String tableName) {
        String sql = "SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = '" + tableName.toUpperCase() + "';";
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(sql);
            List<String> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString(1));
            }
            columns.remove("TIMESTAMP");
            columns.remove("OPEN");
            columns.remove("HIGH");
            columns.remove("LOW");
            columns.remove("CLOSE");
            columns.remove("VOLUME");
            columns.remove("DATE");
            columns.remove("ID");
            for (String column : columns) {
                String dropSql = "ALTER TABLE " + tableName + " DROP COLUMN \"" + column + "\";";
                this.conn.createStatement().execute(dropSql);
            }
            System.out.println("Table cleaned successfully.");
        } catch (SQLException e) {
            System.out.println("Error cleaning table: " + e.getMessage());
        }
    }

    /**
     * Deletes the specified table from the database.
     * @param tableName the name of the table to delete
     */
    public void deleteTable(String tableName) {
        try {
            String sql = "DROP TABLE " + tableName + ";";
            this.conn.createStatement().execute(sql);
            System.out.println("Table deleted successfully.");
        } catch (SQLException e) {
            System.out.println("Error deleting table: " + e.getMessage());
        }
    }

    /**
     * Retrieves the result set for the specified query.
     * @param query the SQL query to execute
     * @return the result set of the query
     */
    public ResultSet getResultSet(String query) {
        try {
            return conn.createStatement().executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
            return null;
        }
    }

    /**
     * Sets the fetch size for the result set.
     * @param size the fetch size to set
     */
    public void setFetchSize(int size) {
        try {
            conn.createStatement().setFetchSize(size);
        } catch (SQLException e) {
            System.out.println("Error setting fetch size: " + e.getMessage());
        }
    }

    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        try {
            this.conn.close();
            this.conn = null;
            System.out.println("Connection closed.");
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Executes the specified SQL query.
     * @param query the SQL query to execute
     */
    public void executeQuery(String query) {
        try {
            this.conn.createStatement().execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts data from a CSV file into the specified table.
     * @param tableName the name of the table to insert data into
     * @param csvFilePath the path to the CSV file
     * @throws FileNotFoundException if the CSV file is not found
     */
    public void insertCsvData(String tableName, String csvFilePath) throws FileNotFoundException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                System.out.println("FAIL - Empty CSV file.");
                return;
            }
            String[] csvHeaders = headerLine.toLowerCase().split(",");
            List<String> dbColumns = Arrays.asList("timestamp", "open", "high", "low", "close", "volume", "date");
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "timestamp BIGINT,"
                    + "open DOUBLE,"
                    + "high DOUBLE,"
                    + "low DOUBLE,"
                    + "close DOUBLE,"
                    + "volume DOUBLE,"
                    + "date TIMESTAMP"
                    + ");";
            try (Statement stmt = this.conn.createStatement()) {
                stmt.execute(createTableSQL);
            }
            Map<String, Integer> columnIndexMap = new HashMap<>();
            for (int i = 0; i < csvHeaders.length; i++) {
                if (dbColumns.contains(csvHeaders[i])) {
                    columnIndexMap.put(csvHeaders[i], i);
                }
            }
            List<String> matchedColumns = new ArrayList<>(columnIndexMap.keySet());
            if (matchedColumns.isEmpty()) {
                System.out.println("FAIL - No matching columns found.");
                return;
            }
            String sql = "INSERT INTO " + tableName + " (" + String.join(",", matchedColumns) + ") VALUES (" +
                    String.join(",", Collections.nCopies(matchedColumns.size(), "?")) + ")";
            try (PreparedStatement pstmt = this.conn.prepareStatement(sql)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(",");
                    int paramIndex = 1;
                    for (String col : matchedColumns) {
                        int csvIndex = columnIndexMap.get(col);
                        String value = csvIndex < values.length ? values[csvIndex] : null;
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
            System.out.println("SUCCESS - InsertCsvData: " + tableName + " from " + csvFilePath);
        } catch (SQLException | IOException e) {
            System.out.println("FAIL - InsertCsvData: " + e.getMessage());
        }
    }

    /**
     * Inserts columns into the specified table and updates their values.
     * @param tableName the name of the table to insert columns into
     * @param columnMap a map of column names to lists of IdValueRecord objects
     * @return a DataObject containing the result of the operation
     */
    public DataObject insertColumns(String tableName, HashMap<String, List<IdValueRecord>> columnMap) {
        try {
            for (String columnName : columnMap.keySet()) {
                String createColumnQuery = """
                        ALTER TABLE IF EXISTS %s
                        ADD COLUMN IF NOT EXISTS %s DOUBLE DEFAULT 0;
                        """.formatted(tableName, columnName);
                this.executeQuery(createColumnQuery);
            }
            int size = columnMap.values().stream().findFirst().get().size();
            for (int i = 0; i < size; i++) {
                StringBuilder updateSb = new StringBuilder();
                updateSb.append("UPDATE ").append(tableName);
                updateSb.append("\n");
                updateSb.append("SET ");
                for (String columnName : columnMap.keySet()) {
                    updateSb.append(columnName).append(" = ").append(columnMap.get(columnName).get(i).value()).append(", ").append("\n");
                }
                updateSb.deleteCharAt(updateSb.length() - 3);
                updateSb.append("WHERE id = ").append(i + 1).append(";\n");
                String updateSQL = updateSb.toString();
                this.executeQuery(updateSQL);
            }
            return new DataObject(200, "server", "Columns created and updated successfully with batch processing.");
        } catch (Exception e) {
            try {
                this.conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.out.println("Error in batch insertColumns: " + e.getMessage());
            e.printStackTrace();
            return new DataObject(500, "server", "Error creating and updating columns: " + e.getMessage());
        } finally {
            try {
                if (this.conn != null && !this.conn.getAutoCommit()) {
                    this.conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.out.println("Error restoring auto-commit: " + ex.getMessage());
            }
        }
    }

    /**
     * Inserts columns into the specified table and updates their values in batch.
     * @param tableName the name of the table to insert columns into
     * @param columnMap a map of column names to lists of IdValueRecord objects
     * @return a DataObject containing the result of the operation
     */
    public DataObject insertColumnsBatch(String tableName, HashMap<String, List<IdValueRecord>> columnMap) {
        try {
            boolean originalAutoCommit = this.conn.getAutoCommit();
            this.conn.setAutoCommit(false);
            final int BATCH_SIZE = 5000;
            for (String columnName : columnMap.keySet()) {
                String createColumnQuery = """
                    ALTER TABLE IF EXISTS %s
                    ADD COLUMN IF NOT EXISTS %s DOUBLE DEFAULT 0;
                    """.formatted(tableName, columnName);
                this.executeQuery(createColumnQuery);
            }
            int size = columnMap.isEmpty() ? 0 : columnMap.values().iterator().next().size();
            if (size > 0) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("UPDATE ").append(tableName).append(" SET ");
                List<String> columnNames = new ArrayList<>(columnMap.keySet());
                for (int i = 0; i < columnNames.size(); i++) {
                    sqlBuilder.append(columnNames.get(i)).append(" = ?");
                    if (i < columnNames.size() - 1) {
                        sqlBuilder.append(", ");
                    }
                }
                sqlBuilder.append(" WHERE id = ?");
                String updateSQL = sqlBuilder.toString();
                try (PreparedStatement pstmt = this.conn.prepareStatement(updateSQL)) {
                    int batchCount = 0;
                    for (int i = 0; i < size; i++) {
                        for (int colIndex = 0; colIndex < columnNames.size(); colIndex++) {
                            String columnName = columnNames.get(colIndex);
                            double value = columnMap.get(columnName).get(i).value();
                            pstmt.setDouble(colIndex + 1, value);
                        }
                        pstmt.setInt(columnNames.size() + 1, i + 1);
                        pstmt.addBatch();
                        batchCount++;
                        if (batchCount >= BATCH_SIZE || i == size - 1) {
                            pstmt.executeBatch();
                            this.conn.commit();
                            batchCount = 0;
                        }
                    }
                }
            }
            this.conn.commit();
            this.conn.setAutoCommit(originalAutoCommit);
            return new DataObject(200, "server", "Columns created and updated successfully with batch processing.");
        } catch (Exception e) {
            try {
                if (this.conn != null && !this.conn.getAutoCommit()) {
                    this.conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                System.out.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.out.println("Error in batch insertColumns: " + e.getMessage());
            e.printStackTrace();
            return new DataObject(500, "server", "Error creating and updating columns: " + e.getMessage());
        } finally {
            try {
                if (this.conn != null) {
                    this.conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.out.println("Error restoring auto-commit: " + ex.getMessage());
            }
        }
    }
}