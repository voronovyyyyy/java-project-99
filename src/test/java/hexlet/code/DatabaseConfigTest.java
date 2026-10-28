package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import hexlet.code.config.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

class DatabaseConfigTest {
    @Test
    void nativeRenderUrlIsConvertedWithoutLeakingCredentialsToJdbcUrl() {
        var properties = new DataSourceProperties();
        properties.setUrl("postgresql://test:p%40ss%3Aword@db.internal:5432/tasks?sslmode=require");
        try (var source = new DatabaseConfig().dataSource(properties)) {
            assertThat(source.getJdbcUrl())
                    .isEqualTo("jdbc:postgresql://db.internal:5432/tasks?sslmode=require");
            assertThat(source.getUsername()).isEqualTo("test");
            assertThat(source.getPassword()).isEqualTo("p@ss:word");
        }
    }

    @Test
    void standardJdbcUrlIsPreserved() {
        var properties = new DataSourceProperties();
        properties.setUrl("jdbc:h2:mem:config_test");
        try (var source = new DatabaseConfig().dataSource(properties)) {
            assertThat(source.getJdbcUrl()).isEqualTo("jdbc:h2:mem:config_test");
        }
    }

    @Test
    void nativeUrlWithoutExplicitPortAndPassword() {
        var properties = new DataSourceProperties();
        properties.setUrl("postgres://test@localhost/tasks");
        try (var source = new DatabaseConfig().dataSource(properties)) {
            assertThat(source.getJdbcUrl()).isEqualTo("jdbc:postgresql://localhost/tasks");
            assertThat(source.getPassword()).isNull();
        }
    }

    @Test
    void invalidNativeUrlIsRejected() {
        var properties = new DataSourceProperties();
        properties.setUrl("postgresql://localhost/");
        assertThatThrownBy(() -> new DatabaseConfig().dataSource(properties))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
