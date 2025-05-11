package instuments;

import com.github.sol239.javafi.utils.instrument.instruments.EMA;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EMATest {

    @Test
    public void getColumnNamesTest() {
        EMA ema = new EMA();
        String[] expected = {"close"};
        String[] actual = ema.getColumnNames();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getNameTest() {
        EMA ema = new EMA();
        String expected = "ema";
        String actual = ema.getName();
        assertEquals(expected, actual);
    }

    @Test
    public void getDescriptionTest() {
        EMA ema = new EMA();
        String expected = "The Exponential Moving Average (EMA) gives more weight to recent prices, making it more responsive to new information.\n" +
                "Usage: ema:period\n";
        String actual = ema.getDescription();
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest1() {
        EMA ema = new EMA();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0, 3.0, 4.0, 5.0));
        double expected = 4.25;
        double actual = ema.updateRow(prices, 3.0);
        assertEquals(expected, actual, 1e-8);
    }

    @Test
    public void updateRowTest2() {
        EMA ema = new EMA();
        HashMap<String, List<Double>> prices = null;
        double expected = 0;
        double actual = ema.updateRow(prices, 3.0);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest3() {
        EMA ema = new EMA();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0));
        double expected = 0;
        double actual = ema.updateRow(prices, 3.0);
        assertEquals(expected, actual);
    }
}