package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.postgre.DBHandler;

import java.util.List;

public class DelCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "";
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
