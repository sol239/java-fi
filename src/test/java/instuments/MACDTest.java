package instuments;

import com.github.sol239.javafi.utils.instrument.instruments.MACD;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MACDTest {

    @Test
    public void getColumnNamesTest() {
        MACD macd = new MACD();
        String[] expected = {"close"};
        String[] actual = macd.getColumnNames();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getNameTest() {
        MACD macd = new MACD();
        String expected = "macd";
        String actual = macd.getName();
        assertEquals(expected, actual);
    }

    @Test
    public void getDescriptionTest() {
        MACD macd = new MACD();
        String expected = "The Moving Average Convergence Divergence (MACD) is a trend-following momentum indicator that shows the relationship between two moving averages of a security's price.";
        String actual = macd.getDescription();
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest1() {
        MACD macd = new MACD();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 16.0, 17.0, 18.0, 19.0, 20.0, 21.0, 22.0, 23.0, 24.0, 25.0, 26.0));
        double actual = macd.updateRow(prices, 26.0, 12.0, 26.0, 9.0);
        System.out.println(actual);
        assertEquals(7.0, actual);
    }

    @Test
    public void updateRowTest2() {
        MACD macd = new MACD();
        HashMap<String, List<Double>> prices = null;
        double expected = 0;
        double actual = macd.updateRow(prices, 26.0, 12.0, 26.0, 9.0);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRowTest3() {
        MACD macd = new MACD();
        HashMap<String, List<Double>> prices = new HashMap<>();
        prices.put("close", List.of(1.0, 2.0, 3.0, 4.0, 5.0));
        double expected = 0;
        double actual = macd.updateRow(prices, 26.0, 12.0, 26.0, 9.0);
        assertEquals(expected, actual);
    }
}