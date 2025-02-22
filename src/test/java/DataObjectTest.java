import com.github.sol239.javafi.DataObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A class for testing the DataObject class.
 */
public class DataObjectTest {

    /**
     * Test the getNumber method.
     */
    @Test
    public void testGetNumber() {
        DataObject dataObject = new DataObject(1, "client", "cmd");
        assertEquals(1, dataObject.getNumber());
    }

    /**
     * Test the getClientId method.
     */
    @Test
    public void testGetClientId() {
        DataObject dataObject = new DataObject(1, "client", "cmd");
        assertEquals("client", dataObject.getClientId());
    }

    /**
     * Test the getCmd method.
     */
    @Test
    public void testGetCmd() {
        DataObject dataObject = new DataObject(1, "client", "cmd");
        assertEquals("cmd", dataObject.getCmd());
    }

    /**
     * Test the toString method.
     */
    @Test
    public void testToString() {
        DataObject dataObject = new DataObject(1, "client", "cmd");
        assertEquals("[1] | clientID=client | cmd=cmd", dataObject.toString());
    }
}
