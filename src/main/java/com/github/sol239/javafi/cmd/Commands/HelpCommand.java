package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.cmd.Shell;


import java.util.List;

/**
 * Class for help command which prints all available commands_to_load.
 */
public class HelpCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "help";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "The help command prints all available commands.";
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
     * Returns all available commands_to_load using reflection.
     *
     * @param args arguments
     * @return result
     */
    @Override
    public DataObject run(List<String> args, List<String> flags) {
        String path = Shell.COMMANDS_TO_LOAD;
        List<Command> cmds = Shell.loadPlugins(Command.class, path);
        StringBuilder sb = new StringBuilder();
        sb.append("help\n");
        for (Command cmd : cmds) {
            sb.append(cmd.getName());
            sb.append("\n");
        }
        // remove last newline
        sb.deleteCharAt(sb.length() - 1);
        DataObject dataObject = new DataObject(200, "server", sb.toString());
        return dataObject;
    }
}
