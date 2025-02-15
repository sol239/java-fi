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

    public static final String CONFIG_FILE_PATH = "C:\\Users\\david_dyn8g78\\Desktop\\Java\\db_config";
    private final List<String> credentials;
    private String url;
    private String user;
    private String password;

    public DBHandler() {
        this.credentials = loadConfig();
        this.checkCredentials(credentials);
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

        System.out.println("URL: " + url);
        System.out.println("User: " + user);
        System.out.println("Password: " + password);
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
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

}
