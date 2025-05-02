package backtesting;

import com.github.sol239.javafi.utils.backtesting.Setup;
import com.github.sol239.javafi.utils.backtesting.Strategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class StrategyTest {

    @Test
    void testConstructorWithClausesAndSetup() {
        Setup setup = new Setup(1000.0, 2.0, 0.002, 10.0, 5.0, 100.0, 2.0, 3, 60, "2023-01-01", 7200);
        Strategy strategy = new Strategy("open > 1", "close < 2", setup);
        assertEquals("open > 1", strategy.openClause);
        assertEquals("close < 2", strategy.closeClause);
        assertEquals(setup, strategy.setup);
    }

    @Test
    void testConstructorWithSetupOnly() {
        Setup setup = new Setup();
        Strategy strategy = new Strategy(setup);
        assertNull(strategy.openClause);
        assertNull(strategy.closeClause);
        assertEquals(setup, strategy.setup);
    }

    @Test
    void testToString() {
        Setup setup = new Setup(1000.0, 2.0, 0.002, 10.0, 5.0, 100.0, 2.0, 3, 60, "2023-01-01", 7200);
        Strategy strategy = new Strategy("open > 1", "close < 2", setup);
        String str = strategy.toString();
        assertTrue(str.contains("Strategy:"));
        assertTrue(str.contains("openClause = open > 1"));
        assertTrue(str.contains("closeClause = close < 2"));
        assertTrue(str.contains("setup = "));
        assertTrue(str.contains("balance = 1000.0"));
    }

    @Test
    void testLoadClausesFromJson() throws IOException {
        String json = """
        {
          "openClause": "price > 100",
          "closeClause": "price < 90"
        }
        """;
        Path tempFile = Files.createTempFile("strategy", ".json");
        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(json);
        }
        Setup setup = new Setup();
        Strategy strategy = new Strategy(setup);
        strategy.loadClausesFromJson(tempFile.toString());
        assertEquals("price > 100", strategy.openClause);
        assertEquals("price < 90", strategy.closeClause);
        Files.deleteIfExists(tempFile);
    }
}