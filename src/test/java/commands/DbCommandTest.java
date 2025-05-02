package commands;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Commands.DbCommand;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("local-only")
public class DbCommandTest {

    private final DbCommand dbCommand = new DbCommand();

    @Test
    public void getNameTest() {
        assertEquals("db", dbCommand.getName());
    }

    @Test
    public void getDescriptionTest() {
        String expectedDescription = "Usage: db [OPTION]...\n" +
                "The command to check connection to the database.\n" +
                "Options:\n" +
                "  -h, --help";
        assertEquals(expectedDescription, dbCommand.getDescription());
    }

    @Test
    public void getParametersTest() {
        String expectedParameters = "Options:\n" +
                "  -h, --help";
        assertEquals(expectedParameters, dbCommand.getParameters());
    }

    @Test
    public void runTest() {
        DataObject result = dbCommand.run(List.of(), List.of());
        assertEquals("Database connection successful", result.getCmd());
    }



}
