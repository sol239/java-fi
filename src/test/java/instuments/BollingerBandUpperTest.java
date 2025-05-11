package instuments;

import com.github.sol239.javafi.utils.instrument.instruments.BollingerBandUpper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BollingerBandUpperTest {

    @Test
    public void getColumnNamesTest() {
        BollingerBandUpper bbu = new BollingerBandUpper();
        String[] expected = {"close"};
        String[] actual = bbu.getColumnNames();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getNameTest() {
        BollingerBandUpper bbu = new BollingerBandUpper();
        String expected = "bbu";
        String actual = bbu.getName();
        assertEquals(expected, actual);
    }

    @Test
    public void getDescriptionTest() {
        BollingerBandUpper bbu = new BollingerBandUpper();
        String expected = "Bollinger Bands consist of a middle band (SMA) and two outer bands that are two standard deviations away from the middle band.\n" +
                "Usage: bbu:period,multiplier\n";
        String actual = bbu.getDescription();
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest1() {
        BollingerBandUpper bbu = new BollingerBandUpper();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0, 3.0, 4.0, 5.0));
        double expected = 3.0 + 2 * Math.sqrt(2.0);
        double actual = bbu.updateRow(prices, 5.0);
        assertEquals(expected, actual, 1e-8);
    }

    @Test
    public void updateRowTest2() {
        BollingerBandUpper bbu = new BollingerBandUpper();
        HashMap<String, List<Double>> prices = null;
        double expected = 0;
        double actual = bbu.updateRow(prices, 5.0);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest3() {
        BollingerBandUpper bbu = new BollingerBandUpper();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0));
        double expected = 0;
        double actual = bbu.updateRow(prices, 5.0);
        assertEquals(expected, actual);
    }
}