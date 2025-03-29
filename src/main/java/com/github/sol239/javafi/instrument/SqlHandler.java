package com.github.sol239.javafi.instrument;

import com.github.sol239.javafi.postgre.DBHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Deprecated
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
     * @return a list with all the SQL instruments available
     */
    public static List<String> getAllSqlInstruments() {
        List<String> instruments = new ArrayList<>();
        SqlInstruments sqlInstruments = new SqlInstruments();
        for (Method method : sqlInstruments.getClass().getDeclaredMethods()) {
            instruments.add(method.getName());
        }
        return instruments;
    }


}
