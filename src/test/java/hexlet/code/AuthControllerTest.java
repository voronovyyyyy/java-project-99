package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

class AuthControllerTest extends ApiTestSupport {
    @Test
    void loginReturnsWorkingToken() throws Exception {
        var response =
                mvc.perform(
                                post("/api/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"username\":\"hexlet@example.com\",\"password\":\"qwerty\"}"))
                        .andExpect(status().isOk())
                        .andReturn();
        var token = response.getResponse().getContentAsString();
        assertThat(token.split("\\.")).hasSize(3);
        mvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(admin.getEmail()));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"username\":\"nobody@example.com\",\"password\":\"qwerty\"}",
                "{\"username\":\"hexlet@example.com\",\"password\":\"wrong\"}",
                "{}",
                "{\"username\":\"hexlet@example.com\"}",
                "{\"username\":null,\"password\":null}"
            })
    void invalidCredentialsReturn401(String input) throws Exception {
        mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON).content(input))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
        "GET,/api/users",
        "POST,/api/users",
        "GET,/api/users/1",
        "PUT,/api/users/1",
        "DELETE,/api/users/1",
        "GET,/api/task_statuses",
        "POST,/api/task_statuses",
        "GET,/api/task_statuses/1",
        "PUT,/api/task_statuses/1",
        "DELETE,/api/task_statuses/1",
        "GET,/api/tasks",
        "POST,/api/tasks",
        "GET,/api/tasks/1",
        "PUT,/api/tasks/1",
        "DELETE,/api/tasks/1",
        "GET,/api/labels",
        "POST,/api/labels",
        "GET,/api/labels/1",
        "PUT,/api/labels/1",
        "DELETE,/api/labels/1"
    })
    void apiRequiresAuthentication(String method, String path) throws Exception {
        mvc.perform(
                        request(HttpMethod.valueOf(method), path)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mvc.perform(get("/api/users").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void initializerIsIdempotentAndHashesPassword() {
        initializer.run(new org.springframework.boot.DefaultApplicationArguments());
        initializer.run(new org.springframework.boot.DefaultApplicationArguments());
        assertThat(users.count()).isEqualTo(1);
        assertThat(statuses.count()).isEqualTo(5);
        assertThat(statuses.findBySlug("to_review").orElseThrow().getName()).isEqualTo("ToReview");
        assertThat(labels.count()).isEqualTo(2);
        assertThat(encoder.matches("qwerty", admin.getPassword())).isTrue();
        assertThat(admin.getPassword()).isNotEqualTo("qwerty");
        assertThat(statuses.findAll())
                .extracting("slug")
                .containsExactlyInAnyOrder(
                        "draft", "to_review", "to_be_fixed", "to_publish", "published");
        assertThat(labels.findAll()).extracting("name").containsExactlyInAnyOrder("feature", "bug");
        assertThat(admin.getCreatedAt()).isNotNull();
        assertThat(admin.getUpdatedAt()).isNotNull();
    }
}
