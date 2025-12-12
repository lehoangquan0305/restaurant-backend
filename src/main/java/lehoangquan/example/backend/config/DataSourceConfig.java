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
        if (databaseUrl != null) {
            try {
                URI uri = new URI(databaseUrl);
                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
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
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid DATABASE_URL", e);
            }
        }
        // Fallback for local development
        return DataSourceBuilder.create()
                .url("jdbc:postgresql://localhost:5432/restaurant")
                .username("postgres")
                .password("password")
                .type(HikariDataSource.class)
                .build();
    }
}