/**
 * The package provides command utils.
 */
package com.github.sol239.javafi.utils.command.Commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.backtesting.BacktestingExecutor;
import com.github.sol239.javafi.utils.backtesting.Setup;
import com.github.sol239.javafi.utils.backtesting.Strategy;
import com.github.sol239.javafi.utils.command.Command;
import com.google.auto.service.AutoService;

import java.util.List;

@AutoService(Command.class)
public class BtCommand implements Command {

    /**
     * Method to get the name of the command.
     * @return name of the command
     */
    @Override
    public String getName() {
        return "bt";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: bt [OPTION]...\n" +
                "The command to backtest strategies. For more info look into USER.md\n" +
                "Does not support backtesting multiple tables at once.\n" +
                getParameters();
    }

    /**
     * Method to get the parameters of the command.
     *
     * @return parameters of the command
     */
    @Override
    public String getParameters() {
        return "Options:\n" +
                "  -h, --help\n" +
                "  -t=TABLE_NAME, --tables=TABLE_NAME\n" +
                "  -s=STRATEGY_PATH, --strategy=STRATEGY_PATH\n" +
                "  -st=SETUP_PATH, --setup=SETUP_PATH\n" +
                "  -r=RESULT_JSON_PATH, --result=RESULT_JSON_PATH";
    }

    /**
     * Method to run the command.
     *
     * @param args arguments
     * @return result
     */
    @Override
    public DataObject run(List<String> args, List<String> flags) {

        String tableName = null;
        String strategyPath = null;
        String setupPath = null;
        String resultJsonPath = null;

        for (String flag : flags) {
            if (flag.startsWith("-h") || flag.startsWith("--help")) {
                return new DataObject(200, "server", getDescription());
            }
            else if (flag.startsWith("-t=")) {
                tableName = flag.substring(3);
            } else if (flag.startsWith("-s=")) {
                strategyPath = flag.substring(3);
            } else if (flag.startsWith("-st=")) {
                setupPath = flag.substring(4);
            } else if (flag.startsWith("-r=")) {
                resultJsonPath = flag.substring(3);
            }
        }

        try {
            Setup longSetup = Setup.fromJson(setupPath);
            Strategy strategy = new Strategy(longSetup);
            strategy.loadClausesFromJson(strategyPath);

            BacktestingExecutor backtestingExecutor = new BacktestingExecutor();
            backtestingExecutor.addStrategy(strategy);
            backtestingExecutor.createStrategiesColumns(tableName);

            System.out.println(strategy);

            backtestingExecutor.updateStrategiesColumns(tableName);   // TODO: does not have to be executed each time
            DataObject result = backtestingExecutor.run(tableName, longSetup.tradeLifeSpanSeconds, strategy.takeProfit, strategy.stopLoss, resultJsonPath, longSetup.dateRestriction);
            return result;
        } catch (Exception e) {
            return new DataObject(400, "server", "Backtesting failed." );
        }
    }
}
