package com.github.sol239.javafi.utils.command;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Commands.HelpCommand;
import com.github.sol239.javafi.utils.instrument.JavaInstrument;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * Shell class.
 */
public class Shell {
    private HashMap<String, Command> commands;
    private HelpCommand help;
    private Command[] availableCommands;

    public Shell() {
        commands = new HashMap<>();
        help = new HelpCommand();
        commands.put("help", help);

        ServiceLoader<Command> loader = ServiceLoader.load(Command.class);
        this.availableCommands = loader.stream().map(ServiceLoader.Provider::get).toArray(Command[]::new);

        this.addCommandsToHashMap();
    }


    private void addCommandsToHashMap() {
        for (Command command : this.availableCommands) {
            commands.put(command.getName(), command);
        }
    }

    public List<Command> getAvailableCommands() {
        List<Command> availableCommands = new ArrayList<>();
        for (Command command : this.availableCommands) {
            availableCommands.add(command);
        }
        return availableCommands;
    }

    public DataObject runCommand(String commandName, List<String> args, List<String> flags) {
        try {
            Command cmd = this.commands.get(commandName);
            DataObject result = cmd.run(args, flags);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Is this the correct way to handle this?
            return new DataObject(400, "server", "Command not found.");
        }
    }

}
