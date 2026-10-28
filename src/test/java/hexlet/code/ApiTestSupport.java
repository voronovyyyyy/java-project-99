package hexlet.code;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.component.DataInitializer;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.TokenService;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class ApiTestSupport {
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected UserRepository users;
    @Autowired protected TaskStatusRepository statuses;
    @Autowired protected LabelRepository labels;
    @Autowired protected TaskRepository tasks;
    @Autowired protected PasswordEncoder encoder;
    @Autowired protected TokenService tokens;
    @Autowired private DataInitializer initializer;
    protected User admin;
    protected String authorization;

    @BeforeEach
    void prepareDatabase() {
        tasks.deleteAll();
        labels.deleteAll();
        statuses.deleteAll();
        users.deleteAll();
        initializer.run(new DefaultApplicationArguments());
        admin = users.findByEmail("hexlet@example.com").orElseThrow();
        authorization = "Bearer " + tokens.generate(admin);
    }

    protected User createUser(String email) {
        var user = new User();
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword(encoder.encode("secret"));
        return users.saveAndFlush(user);
    }

    protected Task createTask(String title, String status, User assignee, Label... taskLabels) {
        var task = new Task();
        task.setName(title);
        task.setDescription("Task description");
        task.setIndex(10);
        task.setTaskStatus(statuses.findBySlug(status).orElseThrow());
        task.setAssignee(assignee);
        task.setLabels(new LinkedHashSet<>(Arrays.asList(taskLabels)));
        return tasks.saveAndFlush(task);
    }

    protected JsonNode body(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }
}
