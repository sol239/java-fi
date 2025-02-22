import com.github.sol239.javafi.postgre.DBHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DBHandlerTest {


    @Test
    public void connectTest() {
        DBHandler db = new DBHandler();
        db.closeConnection();
        db.connect();
        assert db.isConnected();
    }

    @Test
    public void isConnectedTest1() {
        DBHandler db = new DBHandler();
        assert db.isConnected();
    }
    @Test
    public void isConnectedTest2() {
        DBHandler db = new DBHandler();
        db.closeConnection();
        assert !db.isConnected();
    }

    @Test
    public void loadConfigTest() {
        DBHandler db = new DBHandler();
        db.loadConfig();

        int expected = 3;
        int actual = db.credentials.size();
        assert expected == actual;
    }

    @Test
    public void getAllTablesTest() {
        DBHandler db = new DBHandler();
        List<String> tables1 = db.getAllTables();
        db.executeQuery("CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(50))");
        List<String> tables2 = db.getAllTables();
        assert tables1.size() + 1 == tables2.size();
    }


}
