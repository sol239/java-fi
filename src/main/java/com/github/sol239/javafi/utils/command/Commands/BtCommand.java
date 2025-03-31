package com.github.sol239.javafi.utils.command.Commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Command;

import java.util.List;

public class BtCommand implements Command {
    @Override
    public String getName() {
        return "bt";
    }

    @Override
    public String getDescription() {
        return "Usage: bt [OPTION]...\n" +
                "The command to backtest strategies. For more info look into USER.md\n" +
                "Does not support backtesting multiple tables at once.\n" +
                "Example: TODO\n" +
                getParameters();
    }

    @Override
    public String getParameters() {
        return "Options:\n" +
                "  -h, --help\n" +
                "  -t=TABLE_NAME, --tables=TABLE_NAME\n" +
                "  -s=STRATEGY_PATH, --strategy=STRATEGY_PATH\n";
    }

    @Override
    public DataObject run(List<String> args, List<String> flags) {
        return new DataObject(200, "server", "Not implemented yet.");
    }
}
