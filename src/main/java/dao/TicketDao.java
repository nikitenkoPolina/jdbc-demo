package dao;

import entity.Ticket;
import exception.DaoException;
import ru.jdbc.util.ConnectionManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * data access object
 * DAO занимается преобразованием реляционной модели в объектную и наоборот.
 * Представляет сообой Singleton: только один объект класса dao нам нужен
 */
public class TicketDao {

    private static final TicketDao INSTANCE = new TicketDao();
    private static final String DELETE_SQL = """
            DELETE FROM ticket
            WHERE id = ?;
            """;

    private static final String SAVE_SQL = """
            INSERT INTO ticket (passenger_no, passenger_name, flight_id, seat_no, cost)
            VALUES (?, ?, ?, ?, ?);
            """;

    private static final String UPDATE_SQL = """
            UPDATE ticket
            SET passenger_name = ?,
                passenger_no = ?,
                flight_id = ?,
                seat_no = ?,
                cost = ?
                    WHERE id = ?;
            """;

    private static final String FIND_ALL = """
                    SELECT id, passenger_no, passenger_name, flight_id, seat_no, cost
                    FROM ticket
                    """;

    private static final String FIND_BY_FLIGHT_ID_SQL = FIND_ALL + """
            WHERE id = ?
            """;

    private TicketDao() {
    }

    public List<Ticket> findAll() {
        List<Ticket> tickets = new ArrayList<>();

        try (
                Connection connection = ConnectionManager.getConnection();
                var pstmt = connection.prepareStatement(FIND_ALL)
                ) {

            pstmt.executeQuery();

            var rs = pstmt.getResultSet();

            while (rs.next()) {
                tickets.add(buildTicket(rs));
            }

            return tickets;

        } catch (SQLException e) {
            throw new DaoException(e);
        }

    }

    public Optional<Ticket> findById(Long id) {
        try (
                Connection connection = ConnectionManager.getConnection();
                var pstmt = connection.prepareStatement(FIND_BY_FLIGHT_ID_SQL);
        ) {

            pstmt.setLong(1, id);

            var rs = pstmt.executeQuery();

            Ticket ticket = null;

            if (rs.next()) {
               buildTicket(rs);
            }

            return Optional.ofNullable(ticket);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void update(Ticket ticket) {
        try (Connection connection = ConnectionManager.getConnection();
             var pstmt = connection.prepareStatement(UPDATE_SQL)) {
            pstmt.setString(1, ticket.getPassengerName());
            pstmt.setString(2, ticket.getPassengerNo());
            pstmt.setLong(3, ticket.getFlightId());
            pstmt.setString(4, ticket.getSeatNo());
            pstmt.setBigDecimal(5, ticket.getCost());
            pstmt.setLong(6, ticket.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Ticket save(Ticket ticket) {
        try (var connection = ConnectionManager.getConnection();
             var pstmt = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ticket.getPassengerNo());
            pstmt.setString(2, ticket.getPassengerName());
            pstmt.setLong(3, ticket.getFlightId());
            pstmt.setString(4, ticket.getSeatNo());
            pstmt.setBigDecimal(5, ticket.getCost());

            pstmt.executeUpdate();

            var generatedKeys = pstmt.getGeneratedKeys();

            if (generatedKeys.next()) {
                ticket.setId(generatedKeys.getLong("id"));
            }

            return ticket;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public static TicketDao getInstance() {
        return INSTANCE;
    }

    public boolean delete(Long id) {

        try (var connection = ConnectionManager.getConnection();
             var pstmt = connection.prepareStatement(DELETE_SQL)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private Ticket buildTicket(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getLong("id"),
                rs.getString("passenger_no"),
                rs.getString("passenger_name"),
                rs.getLong("flight_id"),
                rs.getString("seat_no"),
                rs.getBigDecimal("cost")
        );
    }
}
