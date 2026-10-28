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

class TaskControllerTest extends ApiTestSupport {
    @Test
    void index() throws Exception {
        createTask("First", "draft", admin);
        mvc.perform(get("/api/tasks").header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].title").value("First"));
    }

    @Test
    void showIncludesRelationsAndPublicFieldNames() throws Exception {
        var bug = labels.findByName("bug").orElseThrow();
        var task = createTask("First", "draft", admin, bug);
        mvc.perform(get("/api/tasks/{id}", task.getId()).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First"))
                .andExpect(jsonPath("$.content").value("Task description"))
                .andExpect(jsonPath("$.assignee_id").value(admin.getId()))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(bug.getId()))
                .andExpect(jsonPath("$.name").doesNotExist())
                .andExpect(jsonPath("$.assignee.password").doesNotExist());
    }

    @Test
    void createWithRelations() throws Exception {
        var bug = labels.findByName("bug").orElseThrow();
        var input =
                java.util.Map.of(
                        "title",
                        "New task",
                        "content",
                        "Details",
                        "status",
                        "draft",
                        "assignee_id",
                        admin.getId(),
                        "index",
                        12,
                        "taskLabelIds",
                        java.util.List.of(bug.getId()));
        var response =
                mvc.perform(
                                post("/api/tasks")
                                        .header("Authorization", authorization)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json.writeValueAsString(input)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.title").value("New task"))
                        .andExpect(jsonPath("$.index").value(12))
                        .andReturn();
        var task = tasks.findById(body(response).get("id").asLong()).orElseThrow();
        assertThat(task.getTaskStatus().getSlug()).isEqualTo("draft");
        assertThat(task.getAssignee().getId()).isEqualTo(admin.getId());
        assertThat(task.getLabels()).containsExactly(bug);
    }

    @Test
    void createWithoutOptionalFields() throws Exception {
        mvc.perform(
                        post("/api/tasks")
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Minimal\",\"status\":\"draft\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskLabelIds").isEmpty());
    }

    @Test
    void partialUpdatePreservesOtherFields() throws Exception {
        var task = createTask("Before", "draft", admin);
        mvc.perform(
                        put("/api/tasks/{id}", task.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"After\",\"content\":\"New content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("After"))
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.index").value(10))
                .andExpect(jsonPath("$.assignee_id").value(admin.getId()));
    }

    @Test
    void updateCanChangeStatusLabelsAndClearAssignee() throws Exception {
        var bug = labels.findByName("bug").orElseThrow();
        var feature = labels.findByName("feature").orElseThrow();
        var task = createTask("Before", "draft", admin, bug);
        var input =
                "{\"status\":\"published\",\"assignee_id\":null,\"content\":null,\"index\":42,\"taskLabelIds\":["
                        + feature.getId()
                        + "]}";
        mvc.perform(
                        put("/api/tasks/{id}", task.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(input))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("published"))
                .andExpect(jsonPath("$.taskLabelIds[0]").value(feature.getId()));
        assertThat(task.getAssignee()).isNull();
        assertThat(task.getDescription()).isNull();
    }

    @Test
    void clearLabels() throws Exception {
        var task = createTask("Labelled", "draft", null, labels.findByName("bug").orElseThrow());
        mvc.perform(
                        put("/api/tasks/{id}", task.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"taskLabelIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskLabelIds").isEmpty());
        assertThat(labels.findByName("bug")).isPresent();
    }

    @Test
    void deleteOnlyRemovesTask() throws Exception {
        var task = createTask("Remove", "draft", admin, labels.findByName("bug").orElseThrow());
        mvc.perform(delete("/api/tasks/{id}", task.getId()).header("Authorization", authorization))
                .andExpect(status().isNoContent());
        assertThat(tasks.existsById(task.getId())).isFalse();
        assertThat(users.existsById(admin.getId())).isTrue();
        assertThat(statuses.findBySlug("draft")).isPresent();
        assertThat(labels.findByName("bug")).isPresent();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{}",
                "{\"title\":\"\",\"status\":\"draft\"}",
                "{\"title\":\"Title\",\"status\":\"unknown\"}",
                "{\"title\":\"Title\",\"status\":\"draft\",\"assignee_id\":999999999}",
                "{\"title\":\"Title\",\"status\":\"draft\",\"taskLabelIds\":[999999999]}"
            })
    void invalidCreateReturns400(String input) throws Exception {
        mvc.perform(
                        post("/api/tasks")
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(input))
                .andExpect(status().isBadRequest());
        assertThat(tasks.count()).isZero();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "{\"title\":null}",
                "{\"status\":null}",
                "{\"status\":\"unknown\"}",
                "{\"assignee_id\":999999999}",
                "{\"taskLabelIds\":[999999999]}"
            })
    void invalidUpdateReturns400(String input) throws Exception {
        var task = createTask("Unchanged", "draft", null);
        mvc.perform(
                        put("/api/tasks/{id}", task.getId())
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(input))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingTaskReturns404() throws Exception {
        mvc.perform(get("/api/tasks/999999999").header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }
}
