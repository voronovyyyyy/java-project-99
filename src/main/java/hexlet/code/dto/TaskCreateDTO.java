package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;

public record TaskCreateDTO(
        @NotBlank String title,
        String content,
        Integer index,
        @NotBlank String status,
        @JsonProperty("assignee_id") @Positive Long assigneeId,
        Set<@NotNull @Positive Long> taskLabelIds) {}
