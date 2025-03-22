package com.github.sol239.javafi.backtesting;

import com.github.sol239.javafi.postgre.DBHandler;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TradeDemo {
    public static void main(String[] args) {
        Core core = new Core();
        List<TradingStrategy> activeStrategies = new ArrayList<>();

        // Strategy creation
        String columnName = "rsi14";
        TradingStrategy strategy = new TradingStrategy("btc");
        strategy.addOpen(columnName, 30);
        strategy.addClose(columnName, 70);
        strategy.addColumn("close");
        // --------------------------

        activeStrategies.add(strategy);

        for (TradingStrategy activeStrategy : activeStrategies) {
            String tableName = activeStrategy.getName();
            Set<String> columns = activeStrategy.getColumns();
            HashMap<String, Double> openMap = activeStrategy.getOpenMap();
            HashMap<String, Double> closeMap = activeStrategy.getCloseMap();
            HashMap<String, Double> map = new HashMap<>();


            // iterate over db
            DBHandler db = new DBHandler();
            db.setFetchSize(100);
            String sql = String.format("SELECT * FROM %s ORDER BY date", tableName);
            ResultSet rs = db.getResultSet(sql);

            try {
                while (rs.next()) {
                    for (String column : columns) {
                        map.put(column, rs.getDouble(column));
                    }

                    // open trade
                    boolean open = true;
                    for (String key : openMap.keySet()) {
                        if (map.get(key) > openMap.get(key)) {
                            open = false;
                            break;
                        }
                    }

                    // TODO add to active trades

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
