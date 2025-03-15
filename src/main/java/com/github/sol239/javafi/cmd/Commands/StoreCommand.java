package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.instruments.SqlHandler;
import com.github.sol239.javafi.instruments.SqlInstruments;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class StoreCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "store";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: store [OPTION]...\n" +
                getParameters() + "\n" +
                "The command to store data in the database.\n";
    }

    /**
     * Method to get the parameters of the command.
     *
     * @return parameters of the command
     */
    @Override
    public String getParameters() {
        return "Options:\n" +
                "  -i=, --instruments=INSTRUMENTS\n" +
                "  -t=, --tables=TABLES\n" +
                "  -h, --help\n";
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

        SqlInstruments obj = new SqlInstruments();
        Class<?> cls = obj.getClass();

        // reflection
        for (String tableName : tableArray) {
            for (String methodName : symbols) {
                for (Method method : cls.getDeclaredMethods()) {

                    if (!method.getName().equals(methodName)) {
                        continue;
                    }

                    List<String> _arguments = new ArrayList<>();
                    _arguments.add(tableName);
                    _arguments.addAll(paramsMap.get(methodName));

                    Parameter[] parameters = method.getParameters();
                    Object[] arguments = new Object[parameters.length];

                    for (int i = 0; i < parameters.length; i++) {
                        arguments[i] = convertToType(_arguments.get(i), parameters[i].getType());
                    }

                    for (int i = 0; i < arguments.length - 1; i++) {
                        System.out.print(arguments[i] + ", ");
                    }

                    String result = "";

                    // invoke
                    try {
                        result = (String) method.invoke(obj, arguments);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    SqlHandler.executeQuery(result);
                }
            }
        }
        DataObject dataObject = new DataObject(200, "server", "Operation completed");
        return dataObject;
    }

    /**
     * Convert the input string to the specified type.
     * It's used for reflection to convert the string input to the correct type.
     *
     * @param input the input string
     * @param type  the type to convert to
     * @return the object of the specified type
     */
    public static Object convertToType(String input, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(input);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(input);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(input);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(input);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(input);
        } else if (type == String.class) {
            return input;
        }
        return null;
    }
}
