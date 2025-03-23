package com.github.sol239.javafi.postgre;

import com.github.sol239.javafi.Config;
import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.instrument.java.IdValueRecord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

import static java.sql.DriverManager.getConnection;

/**
 * Class that handles the connection to the database.
 * It can execute queries, check the connection, load the database credentials from the configuration file, etc.
 */
public class DBHandler {

    /**
     * Application's database name.
     */
    public static final String APP_DB_NAME = "java_fi";

    /**
     * Path to the database configuration file.
     */
    public static final String CONFIG_FILE_PATH = Config.CONFIG_FILE;

    /**
     * List of database credentials: url, user, password.
     */
    public final List<String> credentials;
    /**
     * Connection to the database.
     */
    public Connection conn;
    /**
     * Database's url, probably 'jdbc:postgresql://localhost:5432/APP_DB_NAME'.
     */
    private String url;
    /**
     * Database's user, if local postgreSQL database, then probably 'postgres'.
     */
    private String user;
    /**
     * Database's password.
     */
    private String password;
    private Config cfg;

    /**
     * Constructor that loads the database credentials from the configuration file and establishes a connection to the database.
     */
    public DBHandler() {

        this.cfg = new Config();
        this.cfg.loadConfigMap();

        this.credentials = loadConfig();
        this.checkCredentials(credentials);


        if (!checkConnection("jdbc:postgresql://localhost:5432/" + APP_DB_NAME, user, password)) {
            createAppDb();
        }
        this.url = "jdbc:postgresql://localhost:5432/" + APP_DB_NAME;

        this.connect();
        // System.out.println("Connection established: " + checkConnection(url, user, password));
    }

    /**
     * Checks if the connection to the database is successful.
     *
     * @param url      to the database, probably 'jdbc:postgresql://localhost:5432/APP_DB_NAME'
     * @param user     the user, probably 'postgres'
     * @param password the password
     * @return true if connection is successful, false otherwise
     */
    public static boolean checkConnection(String url, String user, String password) {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = getConnection(url, user, password);
            return connection != null;
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the SQL query string for creating a strategy column.
     *
     * @param tableName      Name of the table.
     * @param strategyName   Name of the strategy column.
     * @param strategyClause SQL clause for the strategy.
     * @return SQL query string.
     */
    public static String getSqlStrategyString(String tableName, String strategyName, String strategyClause) {
        String sql = String.format(
                "alter table %s " +
                        "drop column if exists %s; " +

                        "ALTER TABLE %s " +
                        "ADD %s boolean default false; " +

                        "update %s " +
                        "set %s = true " +
                        "where %s; ",
                tableName,
                strategyName,
                tableName,
                strategyName,
                tableName,
                strategyName,
                strategyClause
        );

        return sql;
    }

    /**
     * Establishes a connection to the database.
     */
    public void connect() {
        this.conn = null;
        try {
            this.conn = getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    /**
     * Checks if the connection to the database is successful.
     *
     * @return true if connection is successful, false otherwise.
     */
    public boolean isConnected() {
        return this.conn != null;
    }

    /**
     * Loads the database credentials from the configuration file.
     *
     * @return List of database credentials: url, user, password.
     */
    public List<String> loadConfig() {
        try {
            List<String> config = Files.readAllLines(Paths.get(CONFIG_FILE_PATH));
            return config;
        } catch (IOException e) {
            System.out.println("Error reading the config file: " + e.getMessage());
        }
        return null;
    }

    /**
     * Returns all tables in the database.
     *
     * @return Array of table names.
     */
    public List<String> getAllTables() {
        try {
            DatabaseMetaData dbmd = this.conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, "%", new String[]{"TABLE"});
            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString(3));
            }
            return tables;
        } catch (SQLException e) {
            System.out.println("Error getting tables: " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes all strategies and indicator columns from the database.
     * Only columns timestamp, open, high, low, close, volume, date are preserved.
     *
     * @param tableName Name of the table to clean.
     */
    public void clean(String tableName) {

        // sql query to get all columns in the table
        String sql = "SELECT column_name FROM information_schema.columns WHERE table_name = '" + tableName + "';";
        try {
            ResultSet rs = this.conn.createStatement().executeQuery(sql);
            List<String> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(rs.getString(1));
            }

            // remove all columns that are not in the list
            columns.remove("timestamp");
            columns.remove("open");
            columns.remove("high");
            columns.remove("low");
            columns.remove("close");
            columns.remove("volume");
            columns.remove("date");
            columns.remove("id");


            for (String column : columns) {
                String dropSql = "ALTER TABLE " + tableName + " DROP COLUMN " + column + ";";
                this.conn.createStatement().execute(dropSql);
            }

            System.out.println("Table cleaned successfully.");

        } catch (SQLException e) {
            System.out.println("Error cleaning table: " + e.getMessage());
        }
    }

    /**
     * Deletes the table from the database.
     *
     * @param tableName Name of the table to delete.
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
     * Checks if credentials list is complete.
     *
     * @param credentials List of database credentials: url, user, password.
     */
    public void checkCredentials(List<String> credentials) {
        this.url = this.cfg.configMap.get("url");
        this.user = this.cfg.configMap.get("username");
        this.password = this.cfg.configMap.get("password");
    }

    /**
     * Returns the result set of the query.
     *
     * @param query SQL query.
     * @return Result set of the query.
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
     * Sets the fetch size of the connection.
     * It is used to limit the number of rows fetched from the database - used with result sets.
     *
     * @param size Fetch size.
     */
    public void setFetchSize(int size) {
        try {
            conn.createStatement().setFetchSize(size);
        } catch (SQLException e) {
            System.out.println("Error setting fetch size: " + e.getMessage());
        }
    }

    /**
     * Closes the connection to the database.
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
     * Executes the SQL query.
     *
     * @param query SQL query.
     */
    public void executeQuery(String query) {
        try {
            this.conn.createStatement().execute(query);
        } catch (SQLException e) {
        }
    }

    /**
     * Creates the application database.
     */
    public void createAppDb() {
        String sql = String.format("CREATE DATABASE %s;", APP_DB_NAME);
        try {
            this.conn.createStatement().execute(sql);
            System.out.println("Database created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
    }

    /**
     * Inserts data from a CSV file into the database.
     *
     * @param tableName   Name of the table to insert data into.
     * @param csvFilePath Path to the CSV file.
     * @throws FileNotFoundException if the file is not found
     */
    public void insertCsvData(String tableName, String csvFilePath) throws FileNotFoundException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

            // Read CSV header
            String headerLine = br.readLine();
            if (headerLine == null) {
                System.out.println("FAIL - Empty CSV file.");
                return;
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

            try (Statement stmt = this.conn.createStatement()) {
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
                return;
            }

            String sql = "INSERT INTO " + tableName + " (" + String.join(",", matchedColumns) + ") VALUES (" +
                    String.join(",", Collections.nCopies(matchedColumns.size(), "?")) + ")";

            // Prepare SQL statement
            try (PreparedStatement pstmt = this.conn.prepareStatement(sql)) {
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

            System.out.println("SUCCESS - InsertCsvData: " + tableName + " from " + csvFilePath);

        } catch (SQLException | IOException e) {
            System.out.println("FAIL - InsertCsvData: " + e.getMessage());
        }
    }

    /**
     * Method to insert columns into a table.
     *
     * @param tableName Name of the table.
     * @param columnMap Map of column names and their values. Keys are column names, values are lists of values (double).
     * @return DataObject with status code and message.
     */
    // TODO: testing

    /**
     * Method to insert columns into a table using batch processing for efficiency.
     *
     * @param tableName Name of the table.
     * @param columnMap Map of column names and their values. Keys are column names, values are lists of values (double).
     * @return DataObject with status code and message.
     */
    public DataObject insertColumns(String tableName, HashMap<String, List<IdValueRecord>> columnMap) {
        try {

            for (String columnName : columnMap.keySet()) {
                // Create column if not exists
                String createColumnQuery = """
                        ALTER TABLE IF EXISTS %s
                        ADD COLUMN IF NOT EXISTS %s DOUBLE PRECISION DEFAULT 0;
                        """.formatted(tableName, columnName);
                this.executeQuery(createColumnQuery);
            }

            // iterate over columnMap

            int size = columnMap.values().stream().findFirst().get().size();
            System.out.println("SIZE: " + size);

            for (int i = 0; i < size; i++) {
                StringBuilder updateSb = new StringBuilder();
                updateSb.append("UPDATE ").append(tableName);
                updateSb.append("\n");
                updateSb.append("SET ");
                for (String columnName : columnMap.keySet()) {
                    updateSb.append(columnName).append(" = ").append(columnMap.get(columnName).get(i).value()).append(", ").append("\n");
                }
                // remove last comma ,
                updateSb.deleteCharAt(updateSb.length() - 3);
                updateSb.append("WHERE id = ").append(i + 1).append(";\n");
                String updateSQL = updateSb.toString();
                // System.out.println(updateSQL);
                this.executeQuery(updateSQL);
            }

            return new DataObject(200, "server", "Columns created and updated successfully with batch processing.");
        } catch (Exception e) {
            // Rollback in case of error
            try {
                this.conn.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("Error during rollback: " + rollbackEx.getMessage());
            }

            System.out.println("Error in batch insertColumns: " + e.getMessage());
            e.printStackTrace();
            return new DataObject(500, "server", "Error creating and updating columns: " + e.getMessage());
        } finally {
            // Ensure auto-commit is restored
            try {
                if (this.conn != null && !this.conn.getAutoCommit()) {
                    this.conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.out.println("Error restoring auto-commit: " + ex.getMessage());
            }
        }
    }

    public DataObject insertColumnsBatch(String tableName, HashMap<String, List<IdValueRecord>> columnMap) {
        try {
            // First, disable auto-commit for batch operations
            boolean originalAutoCommit = this.conn.getAutoCommit();
            this.conn.setAutoCommit(false);

            // Define batch size
            final int BATCH_SIZE = 5000;

            // Create all necessary columns first
            for (String columnName : columnMap.keySet()) {
                String createColumnQuery = """
                    ALTER TABLE IF EXISTS %s
                    ADD COLUMN IF NOT EXISTS %s DOUBLE PRECISION DEFAULT 0;
                    """.formatted(tableName, columnName);
                this.executeQuery(createColumnQuery);
            }

            // Validate data size
            int size = columnMap.isEmpty() ? 0 : columnMap.values().iterator().next().size();
            // System.out.println("Processing " + size + " records with batch size " + BATCH_SIZE);

            if (size > 0) {
                // Prepare the parameterized SQL statement for batching
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("UPDATE ").append(tableName).append(" SET ");

                // Add column placeholders
                List<String> columnNames = new ArrayList<>(columnMap.keySet());
                for (int i = 0; i < columnNames.size(); i++) {
                    sqlBuilder.append(columnNames.get(i)).append(" = ?");
                    if (i < columnNames.size() - 1) {
                        sqlBuilder.append(", ");
                    }
                }
                sqlBuilder.append(" WHERE id = ?");

                String updateSQL = sqlBuilder.toString();
                //System.out.println("Prepared statement: " + updateSQL);

                try (PreparedStatement pstmt = this.conn.prepareStatement(updateSQL)) {
                    int batchCount = 0;

                    // Process records in batches
                    for (int i = 0; i < size; i++) {
                        // Set values for each column
                        for (int colIndex = 0; colIndex < columnNames.size(); colIndex++) {
                            String columnName = columnNames.get(colIndex);
                            double value = columnMap.get(columnName).get(i).value();
                            pstmt.setDouble(colIndex + 1, value);
                        }

                        // Set the id for WHERE clause
                        pstmt.setInt(columnNames.size() + 1, i + 1);

                        // Add to batch
                        pstmt.addBatch();
                        batchCount++;

                        // Execute batch when batch size is reached or at the end
                        if (batchCount >= BATCH_SIZE || i == size - 1) {
                            pstmt.executeBatch();
                            this.conn.commit();
                            // System.out.println("Executed and committed batch of " + batchCount + " updates");
                            batchCount = 0;
                        }
                    }
                }
            }

            // Commit any remaining transactions
            this.conn.commit();

            // Restore original auto-commit setting
            this.conn.setAutoCommit(originalAutoCommit);

            return new DataObject(200, "server", "Columns created and updated successfully with batch processing.");
        } catch (Exception e) {
            // Rollback in case of error
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
            // Ensure auto-commit is restored
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
