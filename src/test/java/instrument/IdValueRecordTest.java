package instrument;

import com.github.sol239.javafi.utils.instrument.IdValueRecord;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdValueRecordTest {

    @Test
    void testConstructorAndAccessors() {
        Long id = 14L;
        Double value = 123.45;
        IdValueRecord record = new IdValueRecord(id, value);

        assertEquals(id, record.id());
        assertEquals(value, record.value());
    }

    @Test
    void testToString() {
        IdValueRecord record = new IdValueRecord(14L, 123.45);
        String expected = "ID=14, VALUE=123.45";
        assertEquals(expected, record.toString());
    }
}