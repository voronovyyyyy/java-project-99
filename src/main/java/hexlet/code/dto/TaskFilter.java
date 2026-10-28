package hexlet.code.dto;

import jakarta.validation.constraints.Positive;

public record TaskFilter(
        String titleCont, @Positive Long assigneeId, String status, @Positive Long labelId) {}
