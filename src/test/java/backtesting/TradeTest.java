package backtesting;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.github.sol239.javafi.utils.backtesting.Setup;
import com.github.sol239.javafi.utils.backtesting.Strategy;
import com.github.sol239.javafi.utils.backtesting.Trade;

class TradeTest {

    @Test
    void testConstructorAndFields() {
        Setup setup = new Setup(1000.0, 2.0, 0.002, 10.0, 5.0, 100.0, 2.0, 3, 60, "2023-01-01", 7200);
        Strategy strategy = new Strategy("open > 1", "close < 2", setup);

        Trade trade = new Trade(
                100.5, 110.0, 95.0, 108.0, 50.0,
                "BTCUSDT", "2023-01-01 10:00:00", "2023-01-01 12:00:00", strategy
        );

        assertEquals(100.5, trade.openPrice);
        assertEquals(110.0, trade.takePrice);
        assertEquals(95.0, trade.stopPrice);
        assertEquals(108.0, trade.closePrice);
        assertEquals(50.0, trade.amount);
        assertEquals("BTCUSDT", trade.assetName);
        assertEquals("2023-01-01 10:00:00", trade.openTime);
        assertEquals("2023-01-01 12:00:00", trade.closeTime);
        assertEquals(strategy, trade.strategy);
        assertEquals(0.0, trade.PnL); // default value
    }

    @Test
    void testToString() {
        Setup setup = new Setup();
        Strategy strategy = new Strategy(setup);

        Trade trade = new Trade(
                200.0, 210.0, 190.0, 205.0, 10.0,
                "ETHUSDT", "2023-02-01 09:00:00", "2023-02-01 11:00:00", strategy
        );
        trade.PnL = 12.3456;

        String str = trade.toString();
        assertTrue(str.contains("open = 200.0"));
        assertTrue(str.contains("close = 205.0"));
        assertTrue(str.contains("take = 210.0"));
        assertTrue(str.contains("stop = 190.0"));
        assertTrue(str.contains("amount = 10.0"));
        assertTrue(str.contains("profit = 12.35")); // rounded to 2 decimals
        assertTrue(str.contains("assetName = ETHUSDT"));
        assertTrue(str.contains("openTime = 2023-02-01 09:00:00"));
        assertTrue(str.contains("closeTime = 2023-02-01 11:00:00"));
    }
}