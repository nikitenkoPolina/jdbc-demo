
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.jdbc.util.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcRunner {
    public static void main(String[] args) throws SQLException {

//        Long flightId = 2L;
//        var result = getTicketsByFlightId(flightId);
//        System.out.println(result);

        LocalDateTime start = LocalDate.of(2020, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.now().atStartOfDay();

        System.out.println(getFlightsBetween(start, end));
    }


    final static Logger LOG = LoggerFactory.getLogger(JdbcRunner.class);


    // alt+Enter указать, что мы испольуем postgresql (Inject language or reference)

// DDL example
//        String sql = """
//                    CREATE TABLE IF NOT EXISTS deals (
//                    id SERIAL PRIMARY KEY,
//                    context TEXT NOT NULL
//                );
//                """;

    // DML example
//        String sql = """
//                UPDATE deals
//                SET context = 'Deal autotest with returning operation'
//                WHERE id = 5
//                RETURNING context
//                """;

//        String sql = """
//                SELECT * FROM deals
//                """;
//
//        Class<Driver> driverClass = Driver.class;
//        LOG.info("Устанавливаем соединение с БД");
//        try (Connection conn = ConnectionManager.getConnection();
//             var stmt = conn.createStatement()) {
//            //var executeResult = stmt.execute(sql);
//            //var executeResult = stmt.executeUpdate(sql);
//
//            ResultSet executeResult = stmt.executeQuery(sql);
//
//            System.out.println(executeResult);
//
//            while (executeResult.next()) {
//                System.out.println(executeResult.getInt("id"));
//                System.out.println(executeResult.getString("context"));
//                System.out.println("______");
//            }


//        }


    private static List<Long> getTicketsByFlightId(Long flightId) throws SQLException {
//------- Statement:
//        String sql = """
//                SELECT id
//                FROM ticket
//                WHERE flight_id = %s
//                """.formatted(flightId);

//------PreparedStatement:
        String sql = """
                SELECT id
                FROM ticket
                WHERE flight_id = ?
                """;

        List<Long> result = new ArrayList<>();
        try (var conn = ConnectionManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, flightId);

            var resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                result.add(resultSet.getLong("id"));
                //result.add(resultSet.getObject("id", Long.class)); NULL safe;

            }
            return result;
        }
    }

    private static List<Long> getFlightsBetween(LocalDateTime start, LocalDateTime end) throws SQLException {

        String sql = """
                SELECT id
                FROM flight
                WHERE departure_date BETWEEN ? AND ?
                """;

        List<Long> flights = new ArrayList<>();

        try (var conn = ConnectionManager.getConnection();
             // Использование PreparedStatement для выполнения запросов
             var pstmt = conn.prepareStatement(sql)) {

            System.out.println(pstmt);
            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            System.out.println(pstmt);
            pstmt.setTimestamp(2, Timestamp.valueOf(end));
            System.out.println(pstmt);

            // Извлечение данных с помощью executeQuery and ResultSet
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                flights.add(rs.getLong("id"));
            }
        }

        return flights;
    }
    /** TODO:
    1. Почему pstmt безопаснее stmt? (SQL injection)
     2. var в java
     */


}
