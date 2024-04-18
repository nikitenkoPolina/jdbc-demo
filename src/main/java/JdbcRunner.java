import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.jdbc.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcRunner {
    public static void main(String[] args) throws SQLException {

        final Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);


        String sql = """
                    CREATE TABLE IF NOT EXISTS deals (
                    id SERIAL PRIMARY KEY,
                    context TEXT NOT NULL
                );
                """;

        Class<Driver> driverClass = Driver.class;
        LOG.info("Устанавливаем соединение с БД");
        try (Connection conn = ConnectionManager.getConnection();
             var statement = conn.createStatement()) {
            var executeResult = statement.execute(sql);
            System.out.println(executeResult);

        }
    }
}
