package hexlet.code.component;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final TaskStatusRepository statuses;
    private final LabelRepository labels;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (users.findByEmail("hexlet@example.com").isEmpty()) {
            var user = new User();
            user.setEmail("hexlet@example.com");
            user.setPassword(encoder.encode("qwerty"));
            user.setFirstName("Hexlet");
            user.setLastName("Admin");
            users.save(user);
        }
        var defaults = new LinkedHashMap<String, String>();
        defaults.put("draft", "Draft");
        defaults.put("to_review", "To Review");
        defaults.put("to_be_fixed", "To Be Fixed");
        defaults.put("to_publish", "To Publish");
        defaults.put("published", "Published");
        defaults.forEach(
                (slug, name) -> {
                    if (statuses.findBySlug(slug).isEmpty()) {
                        var status = new TaskStatus();
                        status.setName(name);
                        status.setSlug(slug);
                        statuses.save(status);
                    }
                });
        for (var name : List.of("feature", "bug")) {
            if (labels.findByName(name).isEmpty()) {
                var label = new Label();
                label.setName(name);
                labels.save(label);
            }
        }
    }
}
