package com.github.sol239.javafi.instruments;

public class SqlInstruments {



    public static void  SimpleMovingAverage(String tableName, int period) {

        String query = String.format(


        "ALTER TABLE" + tableName + "DROP COLUMN IF EXISTS sma" + period + ";" +
        "ALTER TABLE" + tableName + "ADD COLUMN sma" + period + " DOUBLE PRECISION;" +

        """
                WITH sma_30 AS (
                SELECT
                datetime,
                close,
                AVG(close) OVER (ORDER BY datetime ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS sma30
                FROM
                btc
        )
        UPDATE btc
        SET sma30 = sma_30.sma30
        FROM sma_30
        WHERE btc.datetime = sma_30.datetime;""");

    }

}
