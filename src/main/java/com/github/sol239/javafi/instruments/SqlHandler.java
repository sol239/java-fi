package com.github.sol239.javafi.instruments;

import com.github.sol239.javafi.postgre.DBHandler;

public class SqlHandler {

    public static void executeQuery(String query) {
        // execute query
        DBHandler dbHandler = new DBHandler();
        dbHandler.executeQuery(query);
        dbHandler.closeConnection();
    }

    public static String[] getAllSqlInstruments() {
        //TODO: implement this method and even in server
        return new String[] {
                "SimpleMovingAverage(String tableName, int period)",
        };
    }


}
