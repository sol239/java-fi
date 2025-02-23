import com.github.sol239.javafi.postgre.DBHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
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

    @Test
    public void cleanTest() {
        // TODO: To be implemented
    }

    @Test
    public void deleteTableTest() {
        DBHandler db = new DBHandler();
        List<String> tables1 = db.getAllTables();
        db.executeQuery("CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(50))");
        List<String> tables2 = db.getAllTables();
        db.deleteTable("test_table");
        List<String> tables3 = db.getAllTables();
        assert tables1.size() == tables3.size();
    }

    @Test
    public void checkCredentialsTest() {
        DBHandler db = new DBHandler();
        try {
            List<String> config = Files.readAllLines(Paths.get(DBHandler.CONFIG_FILE_PATH));
            db.checkCredentials(db.credentials);
            boolean expected = true;
            boolean actual = (config.get(0).equals(db.credentials.get(0)) && config.get(1).equals(db.credentials.get(1)) && config.get(2).equals(db.credentials.get(2)));
            assert expected == actual;
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    // TODO: Add more tests




}
