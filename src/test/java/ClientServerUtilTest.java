import com.github.sol239.javafi.utils.ClientServerUtil;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClientServerUtilTest {
    @Test
    void sendObjectTest() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        String testString = "Test Message";
        ClientServerUtil.sendObject(objectOutputStream, testString);

        assertTrue(byteArrayOutputStream.size() > 0);
    }

    @Test
    void receiveObjectTest() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

        String testString = "Test Message";
        objectOutputStream.writeObject(testString);
        objectOutputStream.flush();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        Object receivedObject = ClientServerUtil.receiveObject(objectInputStream);

        assertNotNull(receivedObject);
        assertEquals(testString, receivedObject);
    }
}
