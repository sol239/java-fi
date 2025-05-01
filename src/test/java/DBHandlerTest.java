import com.github.sol239.javafi.utils.database.DBHandler;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
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
        DBHandler db = new DBHandler();
        try {
            db.insertCsvData("test_table", "src/test/java/eth-daily.csv");
            // TODO: Programmatic interface must be created before testing this method so instruments can be added programmatically.
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        } finally {
            db.deleteTable("test_table");
        }
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

    @Disabled
    @Test
    public void getResultSetTest() {
        DBHandler db = new DBHandler();
        try {
            db.insertCsvData("test_table", "src/test/java/eth-daily.csv");
            ResultSet rs = db.getResultSet("SELECT * FROM eth");
            if (rs == null) {
                assert false;
            } else {
                int i = 0;
                while (rs.next()) {
                    i++;
                    if (i > 10) {
                        break;
                    }
                }
                assert true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        } finally {
            db.deleteTable("test_table");
        }
    }

    @Test
    public void checkConnectionTest() {
        DBHandler db = new DBHandler();
        assert db.conn != null;
        db.closeConnection();
    }

    @Test
    public void executeQueryTest() {
        DBHandler db = new DBHandler();
        try {
            List<String> tables1 = db.getAllTables();
            db.executeQuery("CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(50))");
            List<String> tables2 = db.getAllTables();
            assert tables1.size() + 1 == tables2.size();
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        } finally {
            db.deleteTable("test_table");
        }
    }

    @Disabled
    @Test
    public void createAppDbTest() {
        String sql = "SELECT datname FROM pg_database WHERE datistemplate = false;";
        // Unit testing method requires hardwiring db connection credentials.
    }

    @Test
    public void insertCsvDataTest() {
        DBHandler db = new DBHandler();
        try {
            db.insertCsvData("test_table", "src/test/java/eth-daily.csv");
            ResultSet rs = db.getResultSet("SELECT * FROM test_table");
            while (rs.next()) {
                double close = rs.getDouble("close");
                if (close == 158.61) {
                    assert true;
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        } finally {
            db.deleteTable("test_table");
        }

    }






}
