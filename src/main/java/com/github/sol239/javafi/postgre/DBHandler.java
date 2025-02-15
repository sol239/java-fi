package com.github.sol239.javafi.postgre;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class DBHandler {

    // CONSTANTS
    public static final String APP_DB_NAME = "java_fi";
    public static final String CONFIG_FILE_PATH = "C:\\Users\\david_dyn8g78\\Desktop\\Java\\db_config";

    private final List<String> credentials;
    private String url;
    private String user;
    private String password;

    public DBHandler() {
        this.credentials = loadConfig();
        this.checkCredentials(credentials);

        //TODO: 1) create app db if it doesn't exist
        if (!checkConnection("jdbc:postgresql://localhost:5432/" + APP_DB_NAME, user, password)) {
            createAppDb();
        }
        this.url = "jdbc:postgresql://localhost:5432/" + APP_DB_NAME;

        //TODO: 2) connect to the app db


        System.out.println("Connection established: " + checkConnection(url, user, password));
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

    /**
     * Creates the application database.
     */
    public void createAppDb() {
        String sql = String.format("CREATE DATABASE %s;", APP_DB_NAME);
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.createStatement().execute(sql);
            System.out.println("Database created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
    }

    public void insertCsvData(String tableName, String csvFilePath) {
        String sql = String.format("COPY %s FROM '%s' DELIMITER ',' CSV HEADER;", tableName, csvFilePath);
        try (Connection connection = DriverManager.getConnection(this.url, user, password)) {
            connection.createStatement().execute(sql);
            System.out.println("SUCCESS - Data inserted successfully.");
        } catch (SQLException e) {
            System.out.println("FAIL - Error inserting data: " + e.getMessage());
        }
    }

}
