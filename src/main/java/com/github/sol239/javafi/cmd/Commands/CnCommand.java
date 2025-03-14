package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;

import java.util.List;

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
        return "Checks connection to the server.";
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
     * @param args arguments
     * @return result
     */
    @Override
    public DataObject run(List<String> args, List<String> flags) {
        DataObject dataObject = new DataObject(200, "server", "Connection Open");
        return dataObject;
    }
}
