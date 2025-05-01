package com.github.sol239.javafi.utils.command.Commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Command;
import com.google.auto.service.AutoService;

import java.util.List;
@AutoService(Command.class)
public class CnCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "cn";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: cn [Option]...\n" +
                "Checks the connection to the server.\n" +
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
                "  -h, --help";
    }

    /**
     * Method to run the command.
     *
     * @param args arguments
     * @return result
     */
    @Override
    public DataObject run(List<String> args, List<String> flags) {

        for (String flag : flags) {
            if (flag.startsWith("-h") || flag.startsWith("--help")) {
                return new DataObject(200, "server", getDescription());
            }
        }

        DataObject dataObject = new DataObject(200, "server", "Connection Open");
        return dataObject;
    }
}
