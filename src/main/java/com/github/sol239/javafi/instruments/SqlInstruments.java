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

    public String ema(String tableName, int period) {
        String query = String.format(
                "ALTER TABLE %s DROP COLUMN IF EXISTS ema%d; " +
                        "ALTER TABLE %s ADD COLUMN ema%d DOUBLE PRECISION; " +
                        "WITH ema_data AS ( " +
                        "    SELECT date, close, " +
                        "           AVG(close) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS sma, " +
                        "           (close - LAG(close, 1) OVER (ORDER BY date)) * (2.0 / (%d + 1)) + " +
                        "           LAG(AVG(close) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW), 1) OVER (ORDER BY date) * (1 - (2.0 / (%d + 1))) AS ema%d " +
                        "    FROM %s " +
                        ") " +
                        "UPDATE %s " +
                        "SET ema%d = ema_data.ema%d " +
                        "FROM ema_data " +
                        "WHERE %s.date = ema_data.date;",
                tableName, period, // DROP COLUMN
                tableName, period, // ADD COLUMN
                period - 1, period, period - 1, period, period, tableName, // CTE
                tableName, period, period, // UPDATE
                tableName // WHERE
        );
        return query;
    }

    public String bollingerBands(String tableName, int period, double multiplier) {
        String query = String.format(
                "ALTER TABLE %s DROP COLUMN IF EXISTS bb_middle%d; " +
                        "ALTER TABLE %s DROP COLUMN IF EXISTS bb_upper%d; " +
                        "ALTER TABLE %s DROP COLUMN IF EXISTS bb_lower%d; " +
                        "ALTER TABLE %s ADD COLUMN bb_middle%d DOUBLE PRECISION; " +
                        "ALTER TABLE %s ADD COLUMN bb_upper%d DOUBLE PRECISION; " +
                        "ALTER TABLE %s ADD COLUMN bb_lower%d DOUBLE PRECISION; " +
                        "WITH bb_data AS ( " +
                        "    SELECT date, close, " +
                        "           AVG(close) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS middle_band, " +
                        "           STDDEV(close) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS stddev " +
                        "    FROM %s " +
                        ") " +
                        "UPDATE %s " +
                        "SET bb_middle%d = bb_data.middle_band, " +
                        "    bb_upper%d = bb_data.middle_band + (%f * bb_data.stddev), " +
                        "    bb_lower%d = bb_data.middle_band - (%f * bb_data.stddev) " +
                        "FROM bb_data " +
                        "WHERE %s.date = bb_data.date;",
                tableName, period, // DROP COLUMNS
                tableName, period,
                tableName, period,
                tableName, period, // ADD COLUMNS
                tableName, period,
                tableName, period,
                period - 1, period - 1, tableName, // CTE
                tableName, period, period, multiplier, period, multiplier, // UPDATE
                tableName // WHERE
        );
        return query;
    }

    public String macd(String tableName, int shortPeriod, int longPeriod, int signalPeriod) {
        String query = String.format(
                "ALTER TABLE %s DROP COLUMN IF EXISTS macd_line; " +
                        "ALTER TABLE %s DROP COLUMN IF EXISTS macd_signal; " +
                        "ALTER TABLE %s DROP COLUMN IF EXISTS macd_histogram; " +
                        "ALTER TABLE %s ADD COLUMN macd_line DOUBLE PRECISION; " +
                        "ALTER TABLE %s ADD COLUMN macd_signal DOUBLE PRECISION; " +
                        "ALTER TABLE %s ADD COLUMN macd_histogram DOUBLE PRECISION; " +
                        "WITH ema_short AS ( " +
                        "    SELECT date, close, " +
                        "           AVG(close) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS ema_short " +
                        "    FROM %s " +
                        "), " +
                        "ema_long AS ( " +
                        "    SELECT date, close, " +
                        "           AVG(close) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS ema_long " +
                        "    FROM %s " +
                        "), " +
                        "macd_data AS ( " +
                        "    SELECT date, close, " +
                        "           ema_short.ema_short - ema_long.ema_long AS macd_line, " +
                        "           AVG(ema_short.ema_short - ema_long.ema_long) OVER (ORDER BY date ROWS BETWEEN %d PRECEDING AND CURRENT ROW) AS macd_signal " +
                        "    FROM ema_short " +
                        "    JOIN ema_long USING (date) " +
                        ") " +
                        "UPDATE %s " +
                        "SET macd_line = macd_data.macd_line, " +
                        "    macd_signal = macd_data.macd_signal, " +
                        "    macd_histogram = macd_data.macd_line - macd_data.macd_signal " +
                        "FROM macd_data " +
                        "WHERE %s.date = macd_data.date;",
                tableName, tableName, tableName, // DROP COLUMNS
                tableName, tableName, tableName, // ADD COLUMNS
                shortPeriod - 1, tableName, // ema_short
                longPeriod - 1, tableName, // ema_long
                signalPeriod - 1, // macd_signal
                tableName, // UPDATE
                tableName // WHERE
        );
        return query;
    }




}
