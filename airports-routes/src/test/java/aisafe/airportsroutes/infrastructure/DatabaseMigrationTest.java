package aisafe.airportsroutes.infrastructure;

import org.flywaydb.core.Flyway;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The migrations in db/migration, on a new database and on one Hibernate created before Flyway.
 */
class DatabaseMigrationTest {

    private static String newDatabase() {
        return "jdbc:h2:mem:migration-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
    }

    private static Flyway flyway(String url) {
        return Flyway.configure().dataSource(url, "sa", "").baselineOnMigrate(true).load();
    }

    private static List<String> routes(Connection db) throws SQLException {
        List<String> rows = new ArrayList<>();
        try (Statement st = db.createStatement();
             var rs = st.executeQuery("select id, origin_iata_code, destination_iata_code from route order by id")) {
            while (rs.next()) {
                rows.add(rs.getLong(1) + " " + rs.getString(2) + "-" + rs.getString(3));
            }
        }
        return rows;
    }

    private static void insertRoute(Statement st, String origin, String destination) throws SQLException {
        st.executeUpdate("insert into route (origin_iata_code, destination_iata_code, estimated_flight_time, "
                + "minimum_capacity, minimum_range, status, version) values ('" + origin + "', '" + destination
                + "', 60, 100, 500.0, 'ACTIVE', 0)");
    }

    @Test
    void ensureNewDatabaseGetsEveryMigration() throws SQLException {
        String url = newDatabase();

        flyway(url).migrate();

        try (Connection db = DriverManager.getConnection(url, "sa", ""); Statement st = db.createStatement()) {
            assertEquals("4", flyway(url).info().current().getVersion().getVersion());
            insertRoute(st, "LIS", "OPO");
            assertThrows(SQLException.class, () -> insertRoute(st, "LIS", "OPO"), "one row per origin and destination");
        }
    }

    @Test
    void ensureDatabaseCreatedBeforeFlywayKeepsOneRowPerRouteWithItsHistory() throws Exception {
        String url = newDatabase();
        try (Connection db = DriverManager.getConnection(url, "sa", ""); Statement st = db.createStatement()) {
            // As Hibernate left it: the structure of V1, with a route stored twice
            try (var v1 = getClass().getResourceAsStream("/db/migration/V1__initial_schema.sql")) {
                RunScript.execute(db, new InputStreamReader(v1, StandardCharsets.UTF_8));
            }
            insertRoute(st, "LIS", "OPO");
            insertRoute(st, "LIS", "OPO");
            insertRoute(st, "OPO", "LIS");
            st.executeUpdate("insert into route_history (route_id, change_description, changed_at) values "
                    + "(1, 'created', current_timestamp), (2, 'created', current_timestamp), (3, 'created', current_timestamp)");
        }

        flyway(url).migrate();

        try (Connection db = DriverManager.getConnection(url, "sa", ""); Statement st = db.createStatement()) {
            assertEquals(List.of("1 LIS-OPO", "3 OPO-LIS"), routes(db), "the oldest copy stays");
            try (var rs = st.executeQuery("select route_id from route_history order by route_id")) {
                List<Long> history = new ArrayList<>();
                while (rs.next()) {
                    history.add(rs.getLong(1));
                }
                assertEquals(List.of(1L, 3L), history, "the history of the dropped copy goes with it");
            }
            assertThrows(SQLException.class, () -> insertRoute(st, "LIS", "OPO"));
            assertEquals("4", flyway(url).info().current().getVersion().getVersion());
        }
    }
}
