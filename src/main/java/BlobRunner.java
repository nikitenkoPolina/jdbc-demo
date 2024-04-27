import ru.jdbc.util.ConnectionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;

public class BlobRunner {

    public static void main(String[] args) throws IOException, SQLException {
        // blob - Binary large object (всё, что можно представить в виде байт: картинки, видео, аудио).
        // в postgres: byte array
        // clob - Character Large Object (всё, что можно представить в виде символов)
        // в postgres: TEXT

       loadImage();

    }

    private static void saveImage() throws IOException {
        var sql = """
                UPDATE aircraft
                SET image = ?
                WHERE id = 1
                """;
        try (
        var connection = ConnectionManager.getConnection();
        var stmt = connection.prepareStatement(sql)) {
            stmt.setBytes(1, Files.readAllBytes(Path.of("resources", "Cathay_Pacific_Boeing_777.jpg")));
            stmt.executeUpdate();

    } catch (SQLException e) {
        throw new RuntimeException(e);}
    }

    private static void loadImage() throws IOException, SQLException {
        var sql = """
                SELECT image
                FROM flight_repository.public.aircraft
                WHERE id = ?
                """;
        try (
                var connection = ConnectionManager.getConnection();
                var pstmt = connection.prepareStatement(sql)
        ) {
            pstmt.setInt(1, 1);

            var resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                var image = resultSet.getBytes("image");
                Files.write(Path.of("resources", "Boeing_777.jpg"), image, StandardOpenOption.CREATE);
            }
        }
    }

//    private static void saveImage() throws IOException {
//        var sql = """
//                UPDATE aircraft
//                SET image = ?
//                WHERE id = 1
//                """;
//        try (
//                var connection = ConnectionManager.getConnection();
//                var stmt = connection.prepareStatement(sql)) {
//            connection.setAutoCommit(false);
//            var blob = connection.createBlob();
//            blob.setBytes(1, Files.readAllBytes(Path.of("resources", "Cathay_Pacific_Boeing_777.jpg")));
//            stmt.setBlob(1, blob);
//            stmt.executeUpdate();
//            connection.commit();
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);}
//    }
}
