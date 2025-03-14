package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.postgre.DBHandler;

import java.util.List;

public class DbCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "db";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "The command to check connection to the database.";
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
        DBHandler db;
        try {
            db = new DBHandler();
            if (db.conn != null) {
                DataObject dataObject = new DataObject(200, "server", "Database connection successful");
                db.closeConnection();
                return dataObject;
            } else {
                DataObject errorObject = new DataObject(400, "server", "Database connection failed");
                db.closeConnection();
                return errorObject;
            }

        } catch (Exception e) {
            DataObject errorObject = new DataObject(400, "server", "Database connection failed");
            return errorObject;
        }
    }
}
