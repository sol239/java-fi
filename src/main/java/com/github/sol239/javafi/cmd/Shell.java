package com.github.sol239.javafi.cmd;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.cmd.Commands.HelpCommand;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * Shell class.
 */
public class Shell {
    private HashMap<String, Command> commands;
    private HelpCommand help;
    public static final String COMMANDS_TO_LOAD = "commands_to_load";

    public Shell() {
        commands = new HashMap<>();
        help = new HelpCommand();
        commands.put("help", help);
        this.addCommandsToHashMap(loadPlugins(Command.class, COMMANDS_TO_LOAD));
    }

    /**
     * Načte pluginy pomocí reflexe
     *
     * @param pluginClass daný interface
     * @param pluginNames jména pluginů, které se mají importovat
     * @param <T>         interface
     * @return List<T> pluginů
     */
    public static <T> List<T> loadPlugins(Class<T> pluginClass, String... pluginNames) {
        List<T> plugins = new ArrayList<>();

        for (String pluginName : pluginNames) {
            try {
                // Musíme dát '?' místo 'T'
                Class<?> cls = Class.forName(pluginName);

                // musíme ověřit, zda cls lze přidat do List<T>
                if (cls.isInterface()) {
                    continue;
                }

                // ověříme, zda plugin implementuje daný interace
                if (!pluginClass.isAssignableFrom(cls)) {
                    continue;
                }

                // musíme přetypovat cls na T

                // .newInstance() je deprecated
                // plugins.add((T) cls.newInstance());

                // Kompilátor to přeloží, ale říká, že to je na nás, aby typy seděly.
                // Lze vyřešit:
                // - pomocí @SupressWarnings("unchecked")
                // - pluginClass.cast(cls.getConstructor().newInstance())

                // plugins.add((T) cls.getConstructor().newInstance());   //  --> warning
                plugins.add(pluginClass.cast(cls.getConstructor().newInstance()));

                // Je to rychlejší způsob než pomocí Reflection git API

            } catch (ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return plugins;
    }

    /**
     * Načte pluginy z daného souboru pomocí loadPlugins(Class<T> pluginClass, String... pluginNames)
     *
     * @param pluginClass     daný interface
     * @param pluginsListPath cesta k souboru s názvy pluginů
     * @param <T>             interface
     * @return List<T> pluginů
     */
    public static <T> List<T> loadPlugins(Class<T> pluginClass, String pluginsListPath) {
        List<T> plugins = new ArrayList<>();

        try (Stream<String> lines = Files.lines(Path.of(pluginsListPath))) {
            List<String> pluginNames = lines.toList();
            plugins = loadPlugins(pluginClass, pluginNames.toArray(new String[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return plugins;
    }

    private void addCommandsToHashMap(List<Command> commands) {
        for (Command command : commands) {
            this.commands.put(command.getName(), command);
        }
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
