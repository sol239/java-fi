package com.github.sol239.javafi.cmd.Commands;

import com.github.sol239.javafi.DataObject;
import com.github.sol239.javafi.cmd.Command;
import com.github.sol239.javafi.postgre.DBHandler;

import java.sql.ResultSet;
import java.util.List;

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
        return "Usage: tb \n" +
                "The command to show all available tables\n" +
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

        DBHandler db = new DBHandler();
        StringBuilder sb = new StringBuilder();

        if (args.isEmpty()) {
            String sql = """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public';
                """;
            ResultSet rs = db.getResultSet(sql);



            try {
                while (rs.next()) {
                    sb.append(rs.getString("table_name")).append("\n");
                }
            } catch (Exception e) {
                return new DataObject(400, "server", "Error fetching all table names from DB.");
            }

            sb.deleteCharAt(sb.length() - 1);
        } else if (args.size() == 1) {
            String tableName = args.get(0);
            String sql = """
                    SELECT * FROM %s;
                    """.formatted(tableName);
            ResultSet rs = db.getResultSet(sql);

            try {
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
