package com.github.sol239.javafi.utils.command.Commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Command;
import com.github.sol239.javafi.utils.database.DBHandler;
import com.github.sol239.javafi.utils.instrument.JavaInstrument;
import com.google.auto.service.AutoService;

import java.util.List;

/**
 * A command to clean the database.
 */
@AutoService(Command.class)
public class CleanCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "clean";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: clean [OPTION]...\n" +
                "The command cleans the database - removes ALL strategy and indicator columns.\n" +
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

        DBHandler db = new DBHandler();
        try {
            db.connect();
            List<String> tablesToClean = db.getAllTables();
            for (String table : tablesToClean) {
                db.clean(table);
            }

            DataObject dataObject = new DataObject(200, "server", "Database cleaned");
            return dataObject;
        } catch (Exception e) {
            DataObject errorObject = new DataObject(400, "server", "Database cleaning failed");
            return errorObject;
        } finally {
            db.closeConnection();
        }
    }
}
