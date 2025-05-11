package instuments;

import com.github.sol239.javafi.utils.instrument.instruments.BollingerBandLower;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BollingerBandLowerTest {

    @Test
    public void getColumnNamesTest() {
        BollingerBandLower bbl = new BollingerBandLower();
        String[] expected = {"close"};
        String[] actual = bbl.getColumnNames();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getNameTest() {
        BollingerBandLower bbl = new BollingerBandLower();
        String expected = "bbl";
        String actual = bbl.getName();
        assertEquals(expected, actual);
    }

    @Test
    public void getDescriptionTest() {
        BollingerBandLower bbl = new BollingerBandLower();
        String expected = "Bollinger Bands consist of a middle band (SMA) and two outer bands that are two standard deviations away from the middle band.\n" +
                "Usage: bbl:period,multiplier\n";
        String actual = bbl.getDescription();
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest1() {
        BollingerBandLower bbl = new BollingerBandLower();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0, 3.0, 4.0, 5.0));
        double expected = 3.0 - 2 * Math.sqrt(2.0);
        double actual = bbl.updateRow(prices, 5.0);
        assertEquals(expected, actual, 1e-8);
    }

    @Test
    public void updateRowTest2() {
        BollingerBandLower bbl = new BollingerBandLower();
        HashMap<String, List<Double>> prices = null;
        double expected = 0;
        double actual = bbl.updateRow(prices, 5.0);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest3() {
        BollingerBandLower bbl = new BollingerBandLower();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0));
        double expected = 0;
        double actual = bbl.updateRow(prices, 5.0);
        assertEquals(expected, actual);
    }
}