package com.github.sol239.javafi.utils.command.Commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Command;
import com.github.sol239.javafi.utils.database.DBHandler;
import com.google.auto.service.AutoService;

import java.util.List;

@AutoService(Command.class)
public class DelCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "del";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: del [OPTION]... [TABLE]...\n" +
                "The command to delete a TABLEs from the database.\n" +
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
     * @param args arguments
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

            for (String table : args) {
                db.deleteTable(table);
            }

            DataObject dataObject = new DataObject(200, "server", "Table deleted");
            return dataObject;
        } catch (Exception e) {
            DataObject errorObject = new DataObject(400, "server", "Table deletion failed");
            return errorObject;
        } finally {
            db.closeConnection();
        }
    }
}
