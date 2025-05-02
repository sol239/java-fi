package com.github.sol239.javafi.utils.command.Commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Command;
import com.github.sol239.javafi.utils.instrument.InstrumentExecutor;
import com.github.sol239.javafi.utils.instrument.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.List;

/**
 * A command to print available instruments.
 */
@AutoService(Command.class)
public class InstCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "inst";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: inst \n" +
                "The command prints available instruments\n" +
                getParameters();
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
        try {
            InstrumentExecutor ie = new InstrumentExecutor();
            JavaInstrument[] availableInstruments = ie.getAvailableInstruments();
            StringBuilder sb = new StringBuilder();
            for (JavaInstrument instrument : availableInstruments) {
                sb.append(instrument.getName()).append(" : ").append(instrument.getDescription()).append("\n");
            }
            return new DataObject(200, "server", sb.toString());
        } catch (Exception e) {
            return new DataObject(500, "server", "Error executing command.");
        }

    }
}
