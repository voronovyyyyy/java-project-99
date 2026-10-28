package hexlet.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

class UserControllerTest extends ApiTestSupport {
    @Test
    void indexListsUsersWithoutPasswords() throws Exception {
        createUser("john@example.com");
        mvc.perform(get("/api/users").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void showReturnsUser() throws Exception {
        mvc.perform(get("/api/users/{id}", admin.getId()).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("hexlet@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.createdAt").isString());
    }

    @Test
    void createHashesPassword() throws Exception {
        mvc.perform(
                        post("/api/users")
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"john@example.com\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.password").doesNotExist());
        var user = users.findByEmail("john@example.com").orElseThrow();
        assertThat(encoder.matches("secret", user.getPassword())).isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"email\":\"broken\",\"password\":\"secret\"}",
                "{\"email\":\"john@example.com\",\"password\":\"ab\"}",
                "{\"email\":\"john@example.com\",\"password\":null}"
            })
    void invalidCreateReturns400(String input) throws Exception {
        mvc.perform(
                        post("/api/users")
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(input))
                .andExpect(status().isBadRequest());
        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    void duplicateEmailReturns400() throws Exception {
        mvc.perform(
                        post("/api/users")
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"hexlet@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePreservesMissingFieldsAndClearsExplicitNull() throws Exception {
        var password = admin.getPassword();
        mvc.perform(
                        put("/api/users/{id}", admin.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"firstName\":\"Updated\",\"lastName\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.email").value("hexlet@example.com"));
        var user = users.findById(admin.getId()).orElseThrow();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getPassword()).isEqualTo(password);
    }

    @Test
    void updateCanChangeEmailAndPassword() throws Exception {
        mvc.perform(
                        put("/api/users/{id}", admin.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"email\":\"new@example.com\",\"password\":\"new-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));
        assertThat(
                        encoder.matches(
                                "new-password",
                                users.findById(admin.getId()).orElseThrow().getPassword()))
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"email\":null}",
                "{\"email\":\"bad\"}",
                "{\"password\":\"ab\"}",
                "{\"password\":null}"
            })
    void invalidUpdateReturns400(String input) throws Exception {
        mvc.perform(
                        put("/api/users/{id}", admin.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(input))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cannotEditAnotherUser() throws Exception {
        var user = createUser("another@example.com");
        mvc.perform(
                        put("/api/users/{id}", user.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"firstName\":\"Hacked\"}"))
                .andExpect(status().isForbidden());
        assertThat(user.getFirstName()).isEqualTo("John");
    }

    @Test
    void deleteOwnAccount() throws Exception {
        mvc.perform(delete("/api/users/{id}", admin.getId()).header("Authorization", authorization))
                .andExpect(status().isNoContent());
        assertThat(users.existsById(admin.getId())).isFalse();
    }

    @Test
    void cannotDeleteAnotherUser() throws Exception {
        var user = createUser("another@example.com");
        mvc.perform(delete("/api/users/{id}", user.getId()).header("Authorization", authorization))
                .andExpect(status().isForbidden());
        assertThat(users.existsById(user.getId())).isTrue();
    }

    @Test
    void cannotDeleteAssignedUser() throws Exception {
        createTask("Assigned", "draft", admin);
        mvc.perform(delete("/api/users/{id}", admin.getId()).header("Authorization", authorization))
                .andExpect(status().isBadRequest());
        assertThat(users.existsById(admin.getId())).isTrue();
    }

    @Test
    void missingUserReturns404() throws Exception {
        mvc.perform(get("/api/users/999999999").header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void listSupportsFrontendPaginationAndIdSelection() throws Exception {
        var user = createUser("aaa@example.com");
        mvc.perform(
                        get("/api/users")
                                .param("_sort", "email")
                                .param("_order", "ASC")
                                .param("_start", "0")
                                .param("_end", "1")
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("aaa@example.com"));
        mvc.perform(
                        get("/api/users")
                                .param("id", user.getId().toString())
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
