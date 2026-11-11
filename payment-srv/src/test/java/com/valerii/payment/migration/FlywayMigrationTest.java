package com.valerii.payment.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@Testcontainers
class FlywayMigrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    private Flyway flyway;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();
        flyway.clean();
        jdbc = new JdbcTemplate(new DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));
    }

    @Test
    void migrate_shouldApplyAllMigrations_onEmptyDatabase() {
        MigrateResult result = flyway.migrate();

        assertThat(result.success).isTrue();
        assertThat(result.migrationsExecuted).isPositive();
        assertThat(flyway.info().pending()).isEmpty();
    }

    @Test
    void migrate_shouldCreatePaymentsTable_withExpectedColumns() {
        flyway.migrate();

        List<String> columns = jdbc.queryForList("""
                SELECT column_name FROM information_schema.columns
                WHERE table_name = 'payments'
                """, String.class);

        assertThat(columns).containsExactlyInAnyOrder(
                "id", "order_id", "amount", "currency", "status", "created_at", "updated_at");
    }

    @Test
    void migrate_shouldDoNothing_whenRunTwice() {
        flyway.migrate();

        MigrateResult second = flyway.migrate();

        assertThat(second.migrationsExecuted).isZero();
    }

    @Test
    void validate_shouldPass_afterMigration() {
        flyway.migrate();

        assertThatNoException().isThrownBy(flyway::validate);
    }
}
