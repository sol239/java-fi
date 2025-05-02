package commands;

import com.github.sol239.javafi.utils.command.Commands.CnCommand;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("local-only")
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

        expectedResult = "Usage: cn [Option]...\n" +
                "Checks the connection to the server.\n" +
                "Options:\n" +
                "  -h, --help";
        actualResult = cnCommand.run(List.of(), List.of("-h")).getCmd();
        assertEquals(expectedResult, actualResult);

        expectedResult = "Usage: cn [Option]...\n" +
                "Checks the connection to the server.\n" +
                "Options:\n" +
                "  -h, --help";
        actualResult = cnCommand.run(List.of(), List.of("--help")).getCmd();
        assertEquals(expectedResult, actualResult);

        expectedResult = "Unknown flag.";
        actualResult = cnCommand.run(List.of(), List.of("-x")).getCmd();
        assertEquals(expectedResult, actualResult);
    }
}
