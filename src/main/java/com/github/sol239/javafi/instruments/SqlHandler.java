package com.github.sol239.javafi.instruments;

import com.github.sol239.javafi.postgre.DBHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Just a simple class to handle SQL queries, so sql query can be executed in one line.
 */
public class SqlHandler {

    /**
     * Executes a SQL query
     * @param query the query to be executed
     */
    public static void executeQuery(String query) {
        DBHandler dbHandler = new DBHandler();
        dbHandler.executeQuery(query);
        dbHandler.closeConnection();
    }

    /**
     * Returns all the SQL instruments available
     * @return an array with all the SQL instruments available
     */
    public static List<String> getAllSqlInstruments() {
        List<String> instruments = new ArrayList<>();
        // TODO: implement the logic
        return instruments;

    }


}
