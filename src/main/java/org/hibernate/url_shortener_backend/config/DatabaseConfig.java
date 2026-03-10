package org.hibernate.url_shortener_backend.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        // Try Render's individual variables first
        String pgHost = System.getenv("PGHOST");
        String pgPort = System.getenv("PGPORT");
        String pgDatabase = System.getenv("PGDATABASE");
        String pgUser = System.getenv("PGUSER");
        String pgPassword = System.getenv("PGPASSWORD");

        if (pgHost != null && pgDatabase != null) {
            String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s",
                    pgHost,
                    pgPort != null ? pgPort : "5432",
                    pgDatabase);

            System.out.println("Using Render PostgreSQL:");
            System.out.println("  URL: " + jdbcUrl);
            System.out.println("  User: " + pgUser);

            return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(pgUser)
                    .password(pgPassword)
                    .driverClassName("org.postgresql.Driver")
                    .build();
        }

        // Fallback for local development
        System.out.println("Using H2 in-memory database (development)");
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:urlshortener")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver")
                .build();
    }
}