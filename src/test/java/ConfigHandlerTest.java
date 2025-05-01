import com.github.sol239.javafi.utils.files.ConfigHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ConfigHandlerTest {

    public final String path = System.getProperty("user.dir") + "/src/test/resources/test_config";

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
}
