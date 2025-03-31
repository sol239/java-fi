import com.github.sol239.javafi.utils.DataObject;
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
    public void getNumberTest() {
        DataObject dataObject = new DataObject(1, "client", "cmd");
        assertEquals(1, dataObject.getNumber());
    }

    /**
     * Test the getClientId method.
     */
    @Test
    public void getClientIdTest() {
        DataObject dataObject = new DataObject(1, "client", "cmd");
        assertEquals("client", dataObject.getClientId());
    }

    /**
     * Test the getCmd method.
     */
    @Test
    public void getCmdTest() {
        DataObject dataObject = new DataObject(1, "client", "cmd");
        assertEquals("cmd", dataObject.getCmd());
    }

    /**
     * Test the toString method.
     */
    @Test
    public void toStringTest() {
        DataObject dataObject = new DataObject(1, "client", "cmd");
        assertEquals("[1] | clientID=client | cmd=cmd", dataObject.toString());
    }
}
