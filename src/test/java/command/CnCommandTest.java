package command;

import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Commands.CnCommand;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CnCommandTest {
    private CnCommand cnCommand = new CnCommand();

    @Test
    public void getNameTest() {
        String expectedName = "cn";
        String actualName = cnCommand.getName();
        assertEquals(expectedName, actualName);
    }

    @Test
    public void getDescriptionTest() {
        String expectedDescription = "Usage: cn [Option]...\n" +
                "Checks the connection to the server.\n" +
                "Options:\n" +
                "  -h, --help";
        String actualDescription = cnCommand.getDescription();
        assertEquals(expectedDescription, actualDescription);
    }

    @Test
    public void getParametersTest() {
        String expectedParameters = "Options:\n" +
                "  -h, --help";
        String actualParameters = cnCommand.getParameters();
        assertEquals(expectedParameters, actualParameters);
    }

    @Test
    public void runTest() {
        String expectedResult = "Connection Open";
        String actualResult = cnCommand.run(List.of(), List.of()).getCmd();
        assertEquals(expectedResult, actualResult);
    }


}
