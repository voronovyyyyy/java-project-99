package hexlet.code.config;

import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(DataSourceProperties properties) {
        var url = properties.getUrl();
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            configurePostgres(properties, URI.create(url));
        }
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    private void configurePostgres(DataSourceProperties properties, URI uri) {
        if (uri.getHost() == null || uri.getPath() == null || uri.getPath().equals("/")) {
            throw new IllegalArgumentException("Invalid PostgreSQL connection URL");
        }
        var port = uri.getPort() == -1 ? "" : ":" + uri.getPort();
        var query = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        properties.setUrl("jdbc:postgresql://" + uri.getHost() + port + uri.getRawPath() + query);
        if (uri.getUserInfo() != null) {
            var credentials = uri.getUserInfo().split(":", 2);
            properties.setUsername(credentials[0]);
            properties.setPassword(credentials.length == 2 ? credentials[1] : "");
        }
    }
}
