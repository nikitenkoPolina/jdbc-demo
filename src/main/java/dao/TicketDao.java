package dao;

import dto.TicketFilter;
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
import java.util.stream.Collectors;

/**
 * data access object
 * DAO занимается преобразованием реляционной модели в объектную и наоборот.
 * Представляет сообой Singleton: только один объект класса dao нам нужен
 */
public class TicketDao implements Dao<Long, Ticket> {

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
                    SELECT ticket.id,
                            passenger_no,
                            passenger_name,
                            flight_id,
                            seat_no,
                            cost,
                            f.status,
                            f.flight_no,
                            f.aircraft_id,
                            f.arrival_airport_code,
                            f.arrival_date,
                            f.departure_airport_code,
                            f.departure_date
                                FROM ticket
                                JOIN flight f
                                    ON ticket.flight_id = f.id
                """;

    private static final String FIND_BY_FLIGHT_ID_SQL = FIND_ALL + """
            WHERE ticket.id = ?
            """;

    private final FlightDao flightDao = FlightDao.getInstance();

    private TicketDao() {
    }

    public List<Ticket> findAll(TicketFilter filter) {
        List<Object> parameters = new ArrayList<>();
        List<String> whereParameters = new ArrayList<>();

        if (filter.seatNo() != null) {
            whereParameters.add("seat_no LIKE ?");
            parameters.add("%" + filter.seatNo() + "%");
        }

        if (filter.passengerName() != null) {
            whereParameters.add("passenger_name = ?");
            parameters.add(filter.passengerName());
        }
        parameters.add(filter.limit());
        parameters.add(filter.offset());

        var where = whereParameters.stream().collect(Collectors.joining(
                " AND ", " WHERE ", " LIMIT ? OFFSET ? "));

        var sql = FIND_ALL + where;
        try (
                var connection = ConnectionManager.get();
                var pstmt = connection.prepareStatement(sql)
        ) {
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            System.out.println(pstmt);
            var resultSet = pstmt.executeQuery();
            List<Ticket> tickets = new ArrayList<>();
            while (resultSet.next()) {
                tickets.add(buildTicket(resultSet));
            }

            return tickets;
        } catch (SQLException e) {
            throw new DaoException(e);

        }
    }

    public List<Ticket> findAll() {
        List<Ticket> tickets = new ArrayList<>();

        try (
                Connection connection = ConnectionManager.get();
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
                Connection connection = ConnectionManager.get();
                var pstmt = connection.prepareStatement(FIND_BY_FLIGHT_ID_SQL)
        ) {

            pstmt.setLong(1, id);

            var rs = pstmt.executeQuery();

            Ticket ticket = null;

            if (rs.next()) {
               ticket = buildTicket(rs);
            }

            return Optional.ofNullable(ticket);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void update(Ticket ticket) {
        try (Connection connection = ConnectionManager.get();
             var pstmt = connection.prepareStatement(UPDATE_SQL)) {
            pstmt.setString(1, ticket.getPassengerName());
            pstmt.setString(2, ticket.getPassengerNo());
            pstmt.setLong(3, ticket.getFlight().id());
            pstmt.setString(4, ticket.getSeatNo());
            pstmt.setBigDecimal(5, ticket.getCost());
            pstmt.setLong(6, ticket.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Ticket save(Ticket ticket) {
        try (var connection = ConnectionManager.get();
             var pstmt = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ticket.getPassengerNo());
            pstmt.setString(2, ticket.getPassengerName());
            pstmt.setLong(3, ticket.getFlight().id());
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

        try (var connection = ConnectionManager.get();
             var pstmt = connection.prepareStatement(DELETE_SQL)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private Ticket buildTicket(ResultSet rs) throws SQLException {
//        var flight = new Flight(
//                rs.getLong("flight_id"),
//                rs.getString("flight_no"),
//                rs.getTimestamp("departure_date").toLocalDateTime(),
//                rs.getString("departure_airport_code"),
//                rs.getTimestamp("arrival_date").toLocalDateTime(),
//                rs.getString("arrival_airport_code"),
//                rs.getInt("aircraft_id"),
//                rs.getString("status")
//        );

        return new Ticket(
                rs.getLong("id"),
                rs.getString("passenger_no"),
                rs.getString("passenger_name"),
                flightDao.findById(rs.getLong("flight_id"), rs.getStatement().getConnection()).orElse(null),
                rs.getString("seat_no"),
                rs.getBigDecimal("cost")
        );
    }
}
