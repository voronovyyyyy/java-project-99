package hexlet.code;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskFilterTest extends ApiTestSupport {
    @BeforeEach
    void prepareTasks() {
        var other = createUser("other@example.com");
        var bug = labels.findByName("bug").orElseThrow();
        var feature = labels.findByName("feature").orElseThrow();
        createTask("Create API", "draft", admin, bug);
        createTask("Create UI", "to_review", other, feature);
        createTask("Fix API", "draft", admin, feature);
    }

    @Test
    void titleFilter() throws Exception {
        mvc.perform(
                        get("/api/tasks")
                                .param("titleCont", "CREATE")
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void assigneeFilter() throws Exception {
        mvc.perform(
                        get("/api/tasks")
                                .param("assigneeId", admin.getId().toString())
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void statusFilter() throws Exception {
        mvc.perform(
                        get("/api/tasks")
                                .param("status", "to_review")
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Create UI"));
    }

    @Test
    void labelFilter() throws Exception {
        var feature = labels.findByName("feature").orElseThrow();
        mvc.perform(
                        get("/api/tasks")
                                .param("labelId", feature.getId().toString())
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void filtersCombineWithAnd() throws Exception {
        var bug = labels.findByName("bug").orElseThrow();
        mvc.perform(
                        get("/api/tasks")
                                .param("titleCont", "create")
                                .param("assigneeId", admin.getId().toString())
                                .param("status", "draft")
                                .param("labelId", bug.getId().toString())
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Create API"));
    }

    @Test
    void noMatchesReturnEmptyList() throws Exception {
        mvc.perform(
                        get("/api/tasks")
                                .param("status", "nonexistent")
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void titleTreatsWildcardsLiterally() throws Exception {
        createTask("100%_done", "draft", null);
        mvc.perform(
                        get("/api/tasks")
                                .param("titleCont", "%_")
                                .header("Authorization", authorization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("100%_done"));
    }
}
