package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import hexlet.code.handler.GlobalExceptionHandler;
import io.sentry.Sentry;
import org.junit.jupiter.api.Test;

class ErrorTrackingTest {
    @Test
    void unexpectedErrorIsReportedWithoutExposingDetailsToClient() {
        var exception = new IllegalStateException("Private implementation details");
        try (var sentry = mockStatic(Sentry.class)) {
            var response = new GlobalExceptionHandler().unexpected(exception);
            assertThat(response.getStatusCode().value()).isEqualTo(500);
            assertThat(response.getBody().message()).doesNotContain(exception.getMessage());
            sentry.verify(() -> Sentry.captureException(exception));
        }
    }
}
