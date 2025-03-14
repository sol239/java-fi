package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.postgre.DBHandler;

import java.util.List;

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
        return "The command cleans the database - removes ALL strategy and indicator columns.";
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
