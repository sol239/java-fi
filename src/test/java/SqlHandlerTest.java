import com.github.sol239.javafi.instruments.SqlHandler;
import com.github.sol239.javafi.instruments.SqlInstruments;
import com.github.sol239.javafi.postgre.DBHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SqlHandlerTest {

    /**
     * Test the executeQuery method
     */
    @Test
    public void executeQueryTest() {
        SqlHandler.executeQuery("CREATE TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(50))");

        DBHandler db = new DBHandler();
        List<String> tables = db.getAllTables();
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

    @Test
    public void getAllSqlInstrumentsTest() {
        List<String> instruments = new ArrayList<>();
        List<String> expectedInstruments = new ArrayList<>() {
            {
                add("ema");
                add("rsi");
                add("sma");
                add("macd");
                add("bollingerBands");
            }
        };

        boolean found = true;

        SqlInstruments sqlInstruments = new SqlInstruments();
        for (Method method : sqlInstruments.getClass().getDeclaredMethods()) {
            instruments.add(method.getName());
        }

        for (String instrument : expectedInstruments) {
            if (!instruments.contains(instrument)) {
                found = false;
                break;
            }
        }

        assert (found);
    }
}
