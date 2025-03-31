package com.github.sol239.javafi.utils.cmd.Commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.cmd.Command;

import java.util.List;

public class ExitCommand implements Command {

    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "exit";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: exit [OPTION]...\n" +
                "The command to exit the application and close the connection to the server.\n" +
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
     * @param args  arguments
     * @param flags flags
     * @return result
     */
    @Override
    public DataObject run(List<String> args, List<String> flags) {

        for (String flag : flags) {
            if (flag.startsWith("-h") || flag.startsWith("--help")) {
                return new DataObject(200, "server", getDescription());
            }
        }

        return new DataObject(0, "server", "exit");
    }
}
