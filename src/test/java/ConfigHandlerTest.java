import com.github.sol239.javafi.utils.DataObject;
import com.github.sol239.javafi.utils.files.ConfigHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigHandlerTest {

    public static final String path = System.getProperty("user.dir") + "/src/test/resources/test_config";

    @Test
    public void createConfigFileTemplateTest() throws IOException {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.createConfigFileTemplate(path);

        // get list of files in the directory
        String[] actual = Files.readAllLines(Paths.get(path)).toArray(new String[0]);

        if (actual.length != 3) {
            throw new IOException("Config file template not created correctly");
        } else {
            String[] expected = {
                    "url = ",
                    "password = ",
                    "username = "
            };
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void getConfigParametersTest() {

        List<String> expected = List.of(
                "url",
                "password",
                "username",
                "configMap"
        );

        ConfigHandler configHandler = new ConfigHandler();
        List<String> actual = configHandler.getConfigParameters();

        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void fillConfigMapTest() {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.url = "jdbc:postgresql://localhost:5432/javafi";
        configHandler.password = "password";
        configHandler.username = "postgres";
        configHandler.fillConfigMap();
        assertArrayEquals(new String[]{"jdbc:postgresql://localhost:5432/javafi", "password", "postgres"}, configHandler.configMap.values().toArray());
    }

    @Test
    public void writeConfigMapTest() {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.url = "jdbc:postgresql://localhost:5432/javafi";
        configHandler.password = "password";
        configHandler.username = "postgres";
        configHandler.fillConfigMap();
        configHandler.writeConfigMap(path);

        ConfigHandler configHandler2 = new ConfigHandler();
        configHandler2.loadConfigMap(path);
        assertArrayEquals(new String[]{"jdbc:postgresql://localhost:5432/javafi", "password", "postgres"}, configHandler2.configMap.values().toArray());
    }

    @Test
    public void loadConfigMapTest() {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.loadConfigMap(path);
        assertArrayEquals(new String[]{"jdbc:postgresql://localhost:5432/javafi", "password", "postgres"}, configHandler.configMap.values().toArray());
    }

    @Test
    public void printConfigMapTest() {

        ConfigHandler configHandler = new ConfigHandler();
        ConfigHandler configHandler1 = new ConfigHandler();
        configHandler.url = "jdbc:postgresql://localhost:5432/javafi";
        configHandler.password = "password";
        configHandler.username = "postgres";
        configHandler.fillConfigMap();
        configHandler.writeConfigMap(path);
        configHandler1.loadConfigMap(path);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        PrintStream originalSystemOut = System.out;
        System.setOut(printStream);

        configHandler1.printConfigMap();
        System.setOut(originalSystemOut);

        String expectedOutput = "url = jdbc:postgresql://localhost:5432/javafi\r\n" +
                "password = password\r\n" +
                "username = postgres\r\n";

        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    public void toStringTest() {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.loadConfigMap(path);
        String expected = "url = jdbc:postgresql://localhost:5432/javafi" + "\n" +
                "password = password" + "\n" +
                "username = postgres";
        String actual = configHandler.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void createConfigFileTest() {
        ConfigHandler configHandler = new ConfigHandler();
        DataObject result = new DataObject(200, "server", "Configuration file already exists");
        assertEquals(result.getCmd(), configHandler.createConfigFile(path).getCmd());

        ConfigHandler configHandler2 = new ConfigHandler();
        deleteConfigFile();
        result = new DataObject(200, "server", "Configuration file created successfully");
        assertEquals(result.getCmd(), configHandler2.createConfigFile(path).getCmd());
    }

    @AfterAll
    public static void tearDown() {
        deleteConfigFile();
    }

    public static void deleteConfigFile() {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
