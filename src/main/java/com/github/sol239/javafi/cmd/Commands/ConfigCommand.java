package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class ConfigCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "config";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "The command sets path to the configuration file.";
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
        File configFile = new File(Path.of(args.getFirst()).toString());

        if (configFile.exists()) {
            return new DataObject(200, "server", "Configuration file path set successfully");
        } else {
            return new DataObject(400, "server", "Configuration file does not exist");
        }





    }
}
