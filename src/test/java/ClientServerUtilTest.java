import com.github.sol239.javafi.utils.ClientServerUtil;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void receiveObjectTest() throws IOException {
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

    @Test
    public void createClassTest() {
        ClientServerUtil clientServerUtil = new ClientServerUtil();
        assertNotNull(clientServerUtil);
    }

    @Test
    void sendObjectTestThrowsIOException() throws IOException {
        ObjectOutputStream mockOutputStream = mock(ObjectOutputStream.class);
        Object dummyObject = new Object();
        doThrow(new IOException("Simulated IO Error")).when(mockOutputStream).writeObject(dummyObject);
        ClientServerUtil.sendObject(mockOutputStream, dummyObject);
        verify(mockOutputStream).writeObject(dummyObject);
    }

    @Test
    void receiveObjectTestThrowsIOException() throws IOException, ClassNotFoundException {
        ObjectInputStream mockInputStream = mock(ObjectInputStream.class);
        when(mockInputStream.readObject()).thenThrow(new IOException("Simulated read error"));
        Object result = ClientServerUtil.receiveObject(mockInputStream);
        assertNull(result);
    }

    @Test
    void receiveObjectTestThrowsClassNotFoundException() throws IOException, ClassNotFoundException {
        ObjectInputStream mockInputStream = mock(ObjectInputStream.class);
        when(mockInputStream.readObject()).thenThrow(new ClassNotFoundException("Simulated class not found"));
        Object result = ClientServerUtil.receiveObject(mockInputStream);
        assertNull(result);
    }
}
