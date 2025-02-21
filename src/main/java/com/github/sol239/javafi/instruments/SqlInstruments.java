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

    public String rsi(String tableName, int period) {
        String query = String.format(
                "ALTER TABLE %s DROP COLUMN IF EXISTS rsi%d; " + // Drop the column if it exists
                        "ALTER TABLE %s ADD COLUMN rsi%d DOUBLE PRECISION; " + // Add the column
                        "WITH price_changes AS ( " +
                        "    SELECT date, close, " +
                        "           LAG(close) OVER (ORDER BY date) AS prev_price " +
                        "    FROM %s " +
                        "), " +
                        "gains_losses AS ( " +
                        "    SELECT date, close, " +
                        "           CASE WHEN close - prev_price > 0 THEN close - prev_price ELSE 0 END AS gain, " +
                        "           CASE WHEN close - prev_price < 0 THEN ABS(close - prev_price) ELSE 0 END AS loss " +
                        "    FROM price_changes " +
                        "), " +
                        "avg_gains_losses AS ( " +
                        "    SELECT date, close, " +
                        "           AVG(gain) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS avg_gain, " +
                        "           AVG(loss) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS avg_loss " +
                        "    FROM gains_losses " +
                        "), " +
                        "rsi AS ( " +
                        "    SELECT date, close, " +
                        "           CASE WHEN avg_loss = 0 THEN 100 " +
                        "                ELSE 100 - (100 / (1 + (avg_gain / avg_loss))) END AS rsi%d " +
                        "    FROM avg_gains_losses " +
                        ") " +
                        "UPDATE %s " +
                        "SET rsi%d = rsi.rsi%d " +
                        "FROM rsi " +
                        "WHERE %s.date = rsi.date;",
                tableName, period, // DROP COLUMN
                tableName, period, // ADD COLUMN
                tableName, // price_changes CTE
                period - 1, period - 1, // avg_gains_losses CTE (ROWS BETWEEN)
                period, // rsi CTE
                tableName, period, period, // UPDATE statement
                tableName // WHERE clause
        );

        return query;
    }


}
