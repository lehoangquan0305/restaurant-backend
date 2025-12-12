package lehoangquan.example.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            throw new RuntimeException("DATABASE_URL environment variable is not set. Please ensure the database is attached in Railway.");
        }
        try {
            URI uri = new URI(databaseUrl);
            String host = uri.getHost();
            int port = uri.getPort();
            if (port == -1) {
                port = 5432; // Default PostgreSQL port
            }
            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + uri.getPath();
            if (uri.getUserInfo() != null) {
                String[] userInfo = uri.getUserInfo().split(":");
                String username = userInfo[0];
                String password = userInfo[1];

                return DataSourceBuilder.create()
                        .url(jdbcUrl)
                        .username(username)
                        .password(password)
                        .type(HikariDataSource.class)
                        .build();
            } else {
                throw new RuntimeException("DATABASE_URL does not contain user info.");
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid DATABASE_URL: " + databaseUrl, e);
        }
    }
}