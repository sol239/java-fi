package com.github.sol239.javafi.postgre;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

public class DBHandler {

    // CONSTANTS
    public static final String APP_DB_NAME = "java_fi";
    public static final String CONFIG_FILE_PATH = "C:\\Users\\david_dyn8g78\\Desktop\\Java\\db_config";

    private final List<String> credentials;
    private String url;
    private String user;
    private String password;

    public Connection conn;


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

    public List<String> loadConfig() {
        try {
            List<String> config = Files.readAllLines(Paths.get(CONFIG_FILE_PATH));
            return config;
        } catch (IOException e) {
            System.out.println("Error reading the config file: " + e.getMessage());
        }
        return null;
    }

    public void checkCredentials(List<String> credentials) {
        if (credentials.size() != 3) {
            throw new IllegalArgumentException("Invalid number of credentials.");
        }
        this.url = credentials.get(0);
        this.user = credentials.get(1);
        this.password = credentials.get(2);
    }

    public static boolean checkConnection(String url, String user, String password) {
        /*
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            return true;
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
        */

        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, user, password);
            return connection != null;
        } catch (SQLException | ClassNotFoundException e) {
            //System.out.println("Connection failed: " + e.getMessage());
            return false;
        }


    }

    public void closeConnection() {
        try {
            this.conn.close();
            System.out.println("Connection closed.");
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public void executeQuery(String query) {
        try {
            this.conn.createStatement().execute(query);
            System.out.println("Query executed successfully.");
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
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

        /*
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.createStatement().execute(sql);
            System.out.println("Database created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating database: " + e.getMessage());
        }*/
    }

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

        /*
         try  {

            // create table if not exists
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "timestamp BIGINT,"
                    + "open DOUBLE PRECISION,"
                    + "high DOUBLE PRECISION,"
                    + "low DOUBLE PRECISION,"
                    + "close DOUBLE PRECISION,"
                    + "volume DOUBLE PRECISION,"
                    + "date TIMESTAMP"
                    + ");";

            this.conn.createStatement().execute(sql);


            FileReader fileReader = new FileReader(csvFilePath);

            CopyManager copyManager = new CopyManager((BaseConnection) this.conn);
            String copySql = "COPY " + tableName + " FROM STDIN WITH CSV HEADER DELIMITER ','";
            copyManager.copyIn(copySql, fileReader);

            System.out.println("SUCCESS - InsertCsvData: " + tableName + " from " + csvFilePath);

        } catch (SQLException | IOException e) {
            System.out.println("FAIL - InsertCsvData: " + e.getMessage());
         */
    }
}
