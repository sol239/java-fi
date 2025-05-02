package com.github.sol239.javafi.utils.command;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Commands.HelpCommand;
import com.github.sol239.javafi.utils.instrument.JavaInstrument;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * A Shell class that manages commands.
 */
public class Shell {

    /**
     * A HashMap that stores the commands.
     */
    private final HashMap<String, Command> commands;

    /**
     * A HelpCommand object that provides help information.
     */
    private HelpCommand help;

    /**
     * An array of available commands.
     */
    private final Command[] availableCommands;

    /**
     * A constructor that initializes the Shell object.
     */
    public Shell() {
        commands = new HashMap<>();
        help = new HelpCommand();
        commands.put("help", help);

        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
        this.availableCommands = loader.stream().map(ServiceLoader.Provider::get).toArray(Command[]::new);

        this.addCommandsToHashMap();
    }

    /**
     * A method that loads commands to the hashmap.
     */
    private void addCommandsToHashMap() {
        for (Command command : this.availableCommands) {
            commands.put(command.getName(), command);
        }
    }

    /**
     * A method that returns a list of available commands.
     * @return a list of available commands
     */
    public List<Command> getAvailableCommands() {
        return new ArrayList<>(Arrays.asList(this.availableCommands));
    }

    /**
     * A method that returns a hashmap of commands.
     * @return a hashmap of commands
     */
    public HashMap<String, Command> getCommands() {
        return commands;
    }

    /**
     * A method that runs a command.
     * @param commandName the name of the command
     * @param args the arguments
     * @param flags the flags
     * @return the result of the command
     */
    public DataObject runCommand(String commandName, List<String> args, List<String> flags) {
        try {
            Command cmd = this.commands.get(commandName);
            DataObject result = cmd.run(args, flags);
            return result;
        } catch (Exception e) {
            return new DataObject(400, "server", "Command not found.");
        }
    }


}
