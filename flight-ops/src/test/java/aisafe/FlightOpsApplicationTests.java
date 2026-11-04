package aisafe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FlightOpsApplicationTests {

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void contextLoads() {
	}

	@Test
	void ensureTheDatabaseStructureComesFromTheMigrations() {
		assertEquals("6", jdbc.queryForObject(
				"select max(\"version\") from \"flyway_schema_history\" where \"success\"", String.class));
	}

}
