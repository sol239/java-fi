import com.github.sol239.javafi.instruments.SqlHandler;
import com.github.sol239.javafi.postgre.DBHandler;
import org.junit.jupiter.api.Test;

public class SqlHandlerTest {

    /**
     * Test the executeQuery method
     */
    @Test
    public void executeQueryTest() {
        SqlHandler.executeQuery("CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(50))");

        DBHandler db = new DBHandler();
        String[] tables = db.getAllTables();
        boolean found = false;

        for (String table : tables) {
            if (table.equals("test_table")) {
                found = true;
                break;
            }
        }

        SqlHandler.executeQuery("DROP TABLE test_table");
        assert(found);
    }

}
