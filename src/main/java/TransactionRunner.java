import ru.jdbc.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionRunner {

    public static void main(String[] args) throws SQLException {
        long flightId = 9L;
        String deleteFlightSql = "DELETE FROM flight WHERE id = ?";
        String deleteTicketSql = "DELETE FROM ticket WHERE id = ?";
        Connection connection = null;
        PreparedStatement deleteFlightStatement = null;
        PreparedStatement deleteTicketStatement = null;
        try {
            connection = ConnectionManager.getConnection();
            deleteFlightStatement = connection.prepareStatement(deleteFlightSql);
            deleteTicketStatement = connection.prepareStatement(deleteTicketSql);
                connection.setAutoCommit(false);

                deleteFlightStatement.setLong(1, flightId);
                deleteTicketStatement.setLong(1, flightId);

                deleteTicketStatement.executeUpdate();

                if (true) {
                    throw new RuntimeException("This is a test");
                }
                deleteFlightStatement.executeUpdate();

                connection.commit();

        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            } throw e;
        } finally {
            if (connection != null) {
                connection.close();
            }
            if (deleteFlightStatement != null) {
                deleteFlightStatement.close();
            }
            if (deleteTicketStatement != null) {
                deleteTicketStatement.close();
            }
        }
    }
// Batch запросы:
    // нужен для экономии времени отправки запросов на сторону сервера БД: запросы посылаются сразу всем скопом (батчем)

//    long flightId = 8L;
//    String deleteFlightSql = "DELETE FROM flight WHERE id = " + flightId;
//    String deleteTicketSql = "DELETE FROM ticket WHERE ticket.flight_id = " + flightId;
//
//    Connection connection = null;
//    Statement statement = null;
//
//        try {
//        connection = ConnectionManager.getConnection();
//        connection.setAutoCommit(false); // лучше сразу после открытия соединения;
//
//        statement = connection.createStatement();
    // Выполняем запросы батчем
//        statement.addBatch(deleteTicketSql);
//        statement.addBatch(deleteFlightSql);
// Метод выполняет батч-запросы: выполнятся либо все запросы, либо не выполнится ни одного
//        statement.executeBatch();
//
//        connection.commit();
//
//    } catch (Exception e) {
//        if (connection != null) {
//            connection.rollback();
//        }
//        throw e;
//    } finally {
//        if (connection != null) {
//            connection.close();
//        }
//        if (statement != null) {
//            statement.close();
//        }
//    }
//}
}
