import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.command.Command;
import com.github.sol239.javafi.utils.command.Shell;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShellTest {

    @Test
    public void constructorTest1() {
        Shell shell = new Shell();
        List<Command> availableCommands = shell.getAvailableCommands();
        List<String> actual = availableCommands.stream()
                .map(Command::getName)
                .toList();
        List<String> expected = List.of("bt", "clean", "cn", "config", "db", "del", "exit", "help", "insert", "inst", "st", "tb");
        assertEquals(expected, actual);
    }

    @Test
    public void constructorTest2() {
        Shell shell = new Shell();
        HashMap<String, Command> availableCommands = shell.getCommands();
        List<String> actual = new ArrayList<>(availableCommands.keySet());
        actual.sort(String::compareTo);
        List<String> expected = List.of("bt", "clean", "cn", "config", "db", "del", "exit", "help", "insert", "inst", "st", "tb");
        assertEquals(expected, actual);
    }

    @Test
    public void runCommandTest() {
        Shell shell = new Shell();
        List<String> args = new ArrayList<>();
        List<String> flags = new ArrayList<>();
        String commandName = "help";
        try {
            DataObject result = shell.runCommand(commandName, args, flags);
            String mustHave = "java-fi is an application";
            String message = result.getCmd();
            boolean contains = message.contains(mustHave);
            assertTrue(contains);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
