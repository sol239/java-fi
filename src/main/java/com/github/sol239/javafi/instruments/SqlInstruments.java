package com.github.sol239.javafi.instruments;

import com.github.sol239.javafi.postgre.DBHandler;

import java.lang.reflect.Array;

public class SqlInstruments {

    public String  sma(String tableName, double _period) {

        int period = (int) _period;
        String query = String.format(
                "ALTER TABLE %s DROP COLUMN IF EXISTS sma%d; " +
                        "ALTER TABLE %s ADD COLUMN sma%d DOUBLE PRECISION; " +
                        "WITH sma_%d AS ( " +
                        "    SELECT date, close, " +
                        "           AVG(close) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS sma%d " +
                        "    FROM %s " +
                        ") " +
                        "UPDATE %s " +
                        "SET sma%d = sma_%d.sma%d " +
                        "FROM sma_%d " +
                        "WHERE %s.date = sma_%d.date;",
                tableName, period, // DROP COLUMN
                tableName, period, // ADD COLUMN
                period, period - 1, period, tableName, // CTE (Common Table Expression)
                tableName, period, period, period, period, // UPDATE statement
                tableName, period // WHERE clause
        );

        return query;
    }


}
