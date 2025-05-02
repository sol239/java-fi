package instrument;

import com.github.sol239.javafi.utils.instrument.InstrumentHelper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class InstrumentHelperTest {

    @Test
    void testAddAndStashSizeLimit() {
        InstrumentHelper helper = new InstrumentHelper(3);
        helper.add("col1", 1.0);
        helper.add("col1", 2.0);
        helper.add("col1", 3.0);
        assertEquals(List.of(1.0, 2.0, 3.0), helper.stash.get("col1"));

        // Adding a fourth value should remove the first (1.0)
        helper.add("col1", 4.0);
        assertEquals(List.of(2.0, 3.0, 4.0), helper.stash.get("col1"));
    }

    @Test
    void testAddMultipleColumns() {
        InstrumentHelper helper = new InstrumentHelper(2);
        helper.add("a", 10.0);
        helper.add("b", 20.0);
        assertEquals(List.of(10.0), helper.stash.get("a"));
        assertEquals(List.of(20.0), helper.stash.get("b"));
    }

    @Test
    void testClear() {
        InstrumentHelper helper = new InstrumentHelper(2);
        helper.add("a", 1.0);
        helper.clear();
        assertTrue(helper.stash.isEmpty());
    }

    @Test
    void testLength() {
        InstrumentHelper helper = new InstrumentHelper(5);
        assertEquals(0, helper.length());
        helper.add("x", 1.0);
        helper.add("x", 2.0);
        assertEquals(2, helper.length());
        helper.add("y", 3.0);
        assertEquals(2, helper.length());
    }

    @Test
    void testToString() {
        InstrumentHelper helper = new InstrumentHelper(2);
        helper.add("foo", 1.23);
        String str = helper.toString();
        assertTrue(str.contains("foo"));
        assertTrue(str.contains("1.23"));
    }
}