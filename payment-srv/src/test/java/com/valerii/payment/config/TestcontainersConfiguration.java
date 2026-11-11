package com.valerii.payment.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    public PostgreSQLContainer postgreSQLContainer() {
        return new PostgreSQLContainer("postgres:16")
                .withDatabaseName("payment_db_test")
                .withUsername("postgres")
                .withPassword("postgres");
    }
}
