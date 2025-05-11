package instuments;

import com.github.sol239.javafi.utils.instrument.instruments.Rsi;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RsiTest {

    @Test
    public void getColumnNamesTest() {
        Rsi rsi = new Rsi();
        String[] expected = {"close"};
        String[] actual = rsi.getColumnNames();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getNameTest() {
        Rsi rsi = new Rsi();
        String expected = "rsi";
        String actual = rsi.getName();
        assertEquals(expected, actual);
    }

    @Test
    public void getDescriptionTest() {
        Rsi rsi = new Rsi();
        String expected = "The relative strength index (RSI) is a momentum indicator that measures the magnitude of recent price changes to evaluate overbought or oversold conditions in the price of a stock or other asset.\n" +
                "Usage: rsi:period\n";
        String actual = rsi.getDescription();
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest1() {
        Rsi rsi = new Rsi();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 4.0, 4.0, 4.0, 1.0, 2.0, 3.0, 2.0, 2.0, 2.0));
        double expected = 54.54545454545455;
        double actual = rsi.updateRow(prices, 14.0);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest2() {
        Rsi rsi = new Rsi();
        HashMap<String, List<Double>> prices = null;
        double expected = 0;
        double actual = rsi.updateRow(prices, 14.0);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest3() {
        Rsi rsi = new Rsi();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0, 3.0, 4.0, 2.0, 2.0, 2.0));
        double expected = 0;
        double actual = rsi.updateRow(prices, 14.0);
        assertEquals(expected, actual);
    }


}
