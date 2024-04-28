package dao;

import entity.Flight;
import entity.Ticket;
import exception.DaoException;
import lombok.Data;
import ru.jdbc.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Data
public class FlightDao implements Dao<Long, Flight> {

    private static final FlightDao INSTANCE = new FlightDao();

    private FlightDao() {

    }

    public static FlightDao getInstance() {
        return INSTANCE;
    }

    private static final String FIND_BY_ID_SQL = """
                    SELECT id,
                            status,
                            flight_no,
                            aircraft_id,
                            arrival_airport_code,
                            arrival_date,
                            departure_airport_code,
                            departure_date
                        FROM flight
                     WHERE id = ?
            """;

    @Override
    public boolean delete(Long id) {
        return false;
    }

    @Override
    public Flight save(Ticket ticket) {
        return null;
    }

    @Override
    public List<Flight> findAll() {
        return List.of();
    }

    @Override
    public Optional<Flight> findById(Long id) {
        try (var connection = ConnectionManager.get();) {
            return findById(id, connection);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Optional<Flight> findById(Long id, Connection connection) {
        Flight flight = null;
        try (
             var stmt = connection.prepareStatement(FIND_BY_ID_SQL)
        ) {
            stmt.setLong(1, id);

            var rs = stmt.executeQuery();

            if (rs.next()) {
                flight = new Flight(
                        rs.getLong("id"),
                        rs.getString("flight_no"),
                        rs.getTimestamp("departure_date").toLocalDateTime(),
                        rs.getString("departure_airport_code"),
                        rs.getTimestamp("arrival_date").toLocalDateTime(),
                        rs.getString("arrival_airport_code"),
                        rs.getInt("aircraft_id"),
                        rs.getString("status")
                );
            }
            return Optional.ofNullable(flight);
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void update(Flight ticket) {

    }
}
