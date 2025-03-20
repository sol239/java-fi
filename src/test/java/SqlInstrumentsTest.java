import com.github.sol239.javafi.instrument.SqlInstruments;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A class for testing the SqlInstruments class.
 * Only one method is tested since other methods are too just sql queries returners.
 */
public class SqlInstrumentsTest {

    @Test
    public void testGetAllTables() {
        SqlInstruments sql = new SqlInstruments();
        String expected = "ALTER TABLE eth DROP COLUMN IF EXISTS sma30; ALTER TABLE eth ADD COLUMN sma30 DOUBLE PRECISION; WITH sma_30 AS (     SELECT date, close,            AVG(close) OVER (ORDER BY date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS sma30     FROM eth ) UPDATE eth SET sma30 = sma_30.sma30 FROM sma_30 WHERE eth.date = sma_30.date;";
        String actual = sql.sma("eth", 30);
        assertEquals(expected, actual);
    }
}
