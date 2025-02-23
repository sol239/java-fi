package com.github.sol239.javafi.postgre;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

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
    public static final String CONFIG_FILE_PATH = "C:\\Users\\david_dyn8g78\\Desktop\\Java\\db_config";

    /**
     * List of database credentials: url, user, password.
     */
    public final List<String> credentials;

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

    /**
     * Connection to the database.
     */
    public Connection conn;

    /**
     * Constructor that loads the database credentials from the configuration file and establishes a connection to the database.
     */
    public DBHandler() {
        this.credentials = loadConfig();
        this.checkCredentials(credentials);

        if (!checkConnection("jdbc:postgresql://localhost:5432/" + APP_DB_NAME, user, password)) {
            createAppDb();
        }
        this.url = "jdbc:postgresql://localhost:5432/" + APP_DB_NAME;

        this.connect();
        System.out.println("Connection established: " + checkConnection(url, user, password));
    }

    /**
     * Establishes a connection to the database.
     */
    public void connect() {
        this.conn = null;
        try {
            this.conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    /**
     * Checks if the connection to the database is successful.
     * @return true if connection is successful, false otherwise.
     */
    public boolean isConnected() {
        return this.conn != null;
    }

    /**
     * Loads the database credentials from the configuration file.
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
     * @return Array of table names.
     */
    public List<String> getAllTables() {
        try {
            DatabaseMetaData dbmd = this.conn.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, "%", new String[] {"TABLE"});
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
     * @param credentials List of database credentials: url, user, password.
     */
    public void checkCredentials(List<String> credentials) {
        if (credentials.size() != 3) {
            throw new IllegalArgumentException("Invalid number of credentials.");
        }
        this.url = credentials.get(0);
        this.user = credentials.get(1);
        this.password = credentials.get(2);
    }

    /**
     * Returns the result set of the query.
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
     * Checks if the connection to the database is successful.
     * @param url to the database, probably 'jdbc:postgresql://localhost:5432/APP_DB_NAME'
     * @param user the user, probably 'postgres'
     * @param password the password
     * @return true if connection is successful, false otherwise
     */
    public static boolean checkConnection(String url, String user, String password) {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
            return connection != null;
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
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
     * @param query SQL query.
     */
    public void executeQuery(String query) {
        try {
            this.conn.createStatement().execute(query);
            System.out.println("Query executed successfully.");
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }

    /**
     * Get the SQL query string for creating a strategy column.
     * @param tableName Name of the table.
     * @param strategyName Name of the strategy column.
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
     * @param tableName Name of the table to insert data into.
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

}
