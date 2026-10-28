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

class DictionaryControllerTest extends ApiTestSupport {
    private Long existingId(String resource) {
        return resource.equals("labels")
                ? labels.findByName("bug").orElseThrow().getId()
                : statuses.findBySlug("draft").orElseThrow().getId();
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void index(String resource) throws Exception {
        mvc.perform(get("/api/" + resource).header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].createdAt").isString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void show(String resource) throws Exception {
        mvc.perform(
                        get("/api/{resource}/{id}", resource, existingId(resource))
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingId(resource)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void create(String resource) throws Exception {
        mvc.perform(
                        post("/api/" + resource)
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"New item\",\"slug\":\"new_item\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New item"));
        assertThat(
                        resource.equals("labels")
                                ? labels.findByName("New item").isPresent()
                                : statuses.findBySlug("new_item").isPresent())
                .isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void update(String resource) throws Exception {
        mvc.perform(
                        put("/api/{resource}/{id}", resource, existingId(resource))
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Updated item\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated item"));
        if (resource.equals("task_statuses")) {
            assertThat(statuses.findBySlug("draft")).isPresent();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void deleteUnused(String resource) throws Exception {
        var id = existingId(resource);
        mvc.perform(
                        delete("/api/{resource}/{id}", resource, id)
                                .header("Authorization", authorization))
                .andExpect(status().isNoContent());
        assertThat(resource.equals("labels") ? labels.existsById(id) : statuses.existsById(id))
                .isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void cannotDeleteLinked(String resource) throws Exception {
        var bug = labels.findByName("bug").orElseThrow();
        createTask("Linked", "draft", null, bug);
        mvc.perform(
                        delete("/api/{resource}/{id}", resource, existingId(resource))
                                .header("Authorization", authorization))
                .andExpect(status().isBadRequest());
        assertThat(tasks.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void invalidCreate(String resource) throws Exception {
        mvc.perform(
                        post("/api/" + resource)
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"\",\"slug\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void invalidUpdate(String resource) throws Exception {
        mvc.perform(
                        put("/api/{resource}/{id}", resource, existingId(resource))
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":null}"))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"task_statuses", "labels"})
    void missingResourceReturns404(String resource) throws Exception {
        mvc.perform(
                        get("/api/{resource}/999999999", resource)
                                .header("Authorization", authorization))
                .andExpect(status().isNotFound());
    }

    @Test
    void labelLengthIsValidated() throws Exception {
        for (var name : new String[] {"ab", "a".repeat(1001)}) {
            mvc.perform(
                            post("/api/labels")
                                    .header("Authorization", authorization)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            json.writeValueAsString(
                                                    java.util.Map.of("name", name))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void namesAndSlugsAreUnique() throws Exception {
        mvc.perform(
                        post("/api/labels")
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"bug\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/task_statuses")
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"New name\",\"slug\":\"draft\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/task_statuses")
                                .header("Authorization", authorization)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Draft\",\"slug\":\"new_slug\"}"))
                .andExpect(status().isBadRequest());
    }
}
