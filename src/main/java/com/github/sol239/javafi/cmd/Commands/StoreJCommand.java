package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class StoreJCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "storej";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "";
    }

    /**
     * Method to get the parameters of the command.
     *
     * @return parameters of the command
     */
    @Override
    public String getParameters() {
        return "";
    }

    /**
     * Method to run the command.
     *
     * @param args  arguments
     * @param flags flags
     * @return result
     */
    @Override
    public DataObject run(List<String> args, List<String> flags) {

        String tables = "";
        String instruments = "";
        for (String flag : flags) {
            if (flag.equals("-h") || flag.equals("--help")) {
                DataObject dataObject = new DataObject(200, "server", getDescription());
                return dataObject;
            } else if (flag.startsWith("-i=") || flag.startsWith("--instruments=")) {
                instruments = flag.split("=")[1];
            } else if (flag.startsWith("-t=") || flag.startsWith("--tables=")) {
                tables = flag.split("=")[1];
            }
        }

        String[] tableArray = tables.split(",");
        String[] instrumentArray = instruments.split(",");

        List<String> symbols = new ArrayList<>();
        List<List<String>> argumentsArray = new ArrayList<>();

        for (String x : instrumentArray) {
            String symbol = x.split(":")[0];
            String[] arguments = x.split(":")[1].split(";");

            symbols.add(symbol);
            argumentsArray.add(Arrays.asList(arguments));
        }

        HashMap<String, List<String>> paramsMap = new HashMap<>();
        for (int i = 0; i < symbols.size(); i++) {
            String symbol = symbols.get(i);
            List<String> arguments = argumentsArray.get(i);
            paramsMap.put(symbol, arguments);
        }
        // TODO: store the data
        return new DataObject(200, "server", "Stored");
    }
}
