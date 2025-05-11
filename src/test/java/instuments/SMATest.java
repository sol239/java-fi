package instuments;

import com.github.sol239.javafi.utils.instrument.instruments.SMA;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SMATest {

    @Test
    public void getColumnNamesTest() {
        SMA sma = new SMA();
        String[] expected = {"close"};
        String[] actual = sma.getColumnNames();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getNameTest() {
        SMA sma = new SMA();
        String expected = "sma";
        String actual = sma.getName();
        assertEquals(expected, actual);
    }

    @Test
    public void getDescriptionTest() {
        SMA sma = new SMA();
        String expected = "The Simple Moving Average (SMA) is the unweighted mean of the previous n data points.\n" +
                "Usage: sma:period\n";
        String actual = sma.getDescription();
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest1() {
        SMA sma = new SMA();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0, 3.0, 4.0, 5.0));
        double expected = (3.0 + 4.0 + 5.0) / 3.0;
        double actual = sma.updateRow(prices, 3.0);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest2() {
        SMA sma = new SMA();
        HashMap<String, List<Double>> prices = null;
        double expected = 0;
        double actual = sma.updateRow(prices, 3.0);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest3() {
        SMA sma = new SMA();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0));
        double expected = 0;
        double actual = sma.updateRow(prices, 3.0);
        assertEquals(expected, actual);
    }
}