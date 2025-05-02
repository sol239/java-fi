package backtesting;

import com.github.sol239.javafi.utils.backtesting.Setup;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class SetupTest {

    @Test
    void testConstructorAndFields() {
        Setup setup = new Setup(1000.0, 2.0, 0.002, 10.0, 5.0, 100.0, 2.0, 3, 60, "2023-01-01", 7200);
        assertEquals(1000.0, setup.balance);
        assertEquals(2.0, setup.leverage);
        assertEquals(0.002, setup.fee);
        assertEquals(10.0, setup.takeProfit);
        assertEquals(5.0, setup.stopLoss);
        assertEquals(100.0, setup.amount);
        assertEquals(2.0, setup.riskReward);
        assertEquals(3, setup.maxTrades);
        assertEquals(60, setup.delaySeconds);
        assertEquals("2023-01-01", setup.dateRestriction);
        assertEquals(0, setup.maxOpenedTrades);
        assertEquals(7200, setup.tradeLifeSpanSeconds);
    }

    @Test
    void testToString() {
        Setup setup = new Setup(1000.0, 2.0, 0.002, 10.0, 5.0, 100.0, 2.0, 3, 60, "2023-01-01", 7200);
        String str = setup.toString();
        assertTrue(str.contains("balance = 1000.0"));
        assertTrue(str.contains("leverage = 2.0"));
        assertTrue(str.contains("fee = 0.002"));
        assertTrue(str.contains("takeProfit = 10.0"));
        assertTrue(str.contains("stopLoss = 5.0"));
        assertTrue(str.contains("amount = 100.0"));
        assertTrue(str.contains("riskReward = 2.0"));
        assertTrue(str.contains("maxTrades = 3"));
        assertTrue(str.contains("delaySeconds = 60"));
        assertTrue(str.contains("maxOpenedTrades = 0"));
        assertTrue(str.contains("dateRestriction = 2023-01-01"));
        assertTrue(str.contains("tradeLifeSpanSeconds = 7200"));
    }

    @Test
    void testImportFromAssetsSetup1Json() {
        // Adjust the path if needed depending on your test/resources setup
        String jsonPath = "assets/setups/setup_1.json";
        Setup setup = Setup.fromJson(jsonPath);
        assertNotNull(setup);
        assertEquals(10000.0, setup.balance);
        assertEquals(12.0, setup.leverage);
        assertEquals(0.0025, setup.fee);
        assertEquals(1.01, setup.takeProfit);
        assertEquals(0.99, setup.stopLoss);
        assertEquals(500.0, setup.amount);
        assertEquals(5, setup.maxTrades);
        assertEquals(10800, setup.delaySeconds);
        assertEquals("", setup.dateRestriction);
        assertEquals(90000, setup.tradeLifeSpanSeconds);
    }

    @Test
    void testFromJsonMissingOptionalFields() throws IOException {
        String json = """
        {
          "balance": 1000.0,
          "leverage": 2.0,
          "fee": 0.002,
          "takeProfit": 10.0,
          "stopLoss": 5.0,
          "amount": 100.0,
          "maxTrades": 3,
          "delaySeconds": 60,
          "dateRestriction": "2023-01-01"
        }
        """;
        Path tempFile = Files.createTempFile("setup", ".json");
        try (FileWriter writer = new FileWriter(tempFile.toFile())) {
            writer.write(json);
        }
        Setup setup = Setup.fromJson(tempFile.toString());
        assertNotNull(setup);
        assertEquals(10.0 / 5.0, setup.riskReward);
        assertEquals(3600 * 24, setup.tradeLifeSpanSeconds);
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testFromJsonFileNotFound() {
        Setup setup = Setup.fromJson("nonexistent_file.json");
        assertNull(setup);
    }
}