package hexlet.code.component;

import hexlet.code.handler.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.monitoring-check", havingValue = "true")
public class MonitoringCheck implements ApplicationRunner {
    private final GlobalExceptionHandler errors;

    @Override
    public void run(ApplicationArguments arguments) {
        errors.unexpected(new IllegalStateException("Task Manager: error tracking verification"));
    }
}
