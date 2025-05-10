package com.github.sol239.javafi.utils.command.Commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Command;
import com.github.sol239.javafi.utils.database.DBHandler;
import com.google.auto.service.AutoService;

import java.sql.ResultSet;
import java.util.List;

/**
 * A command to show all available tables in the database.
 */
@AutoService(Command.class)
public class TbCommand implements Command {
    /**
     * Method to get the name of the command.
     *
     * @return name of the command
     */
    @Override
    public String getName() {
        return "tb";
    }

    /**
     * Method to get the description of the command.
     *
     * @return description of the command
     */
    @Override
    public String getDescription() {
        return "Usage: tb [OPTIONAL]\n" +
                "The command to show all available tables\n" +
                "If table name is provided, it will show the data in the table.\n" +
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
        System.out.println("Executing command: " + getName());
        System.out.println("Arguments: " + args + "\n");
        System.out.println("Flags: " + flags + "\n");

        for (String flag : flags) {
            if (flag.equals("-h") || flag.equals("--help")) {
                return new DataObject(200, "server", this.getDescription());
            }
        }

        DBHandler db = new DBHandler();
        StringBuilder sb = new StringBuilder();

        if (args.isEmpty()) {
            List<String> tables = db.getAllTables();
            if (tables == null || tables.isEmpty()) {
                return new DataObject(400, "server", "No tables found in DB.");
            }
            for (String table : tables) {
                sb.append(table).append("\n");
            }
            sb.deleteCharAt(sb.length() - 1);
        } else if (args.size() == 1) {
            String tableName = args.get(0);
            String sql = "SELECT * FROM " + tableName + ";";
            ResultSet rs = db.getResultSet(sql);

            try {
                if (rs == null) {
                    return new DataObject(400, "server", "Table not found or error executing query.");
                }
                // COLUMN NAMES
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    sb.append(rs.getMetaData().getColumnName(i)).append(" ");
                }
                sb.append("\n");

                // DATA
                while (rs.next()) {
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                        sb.append(rs.getString(i)).append(" ");
                    }
                    sb.append("\n");
                }
            } catch (Exception e) {
                return new DataObject(400, "server", "Error printing the table.");
            }
        }

        return new DataObject(200, "server", sb.toString());
    }
}
